package org.folio.services.impl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.HttpStatus;
import org.folio.kafka.PubSubKafkaConfig;
import org.folio.kafka.PubSubConfig;
import org.folio.rest.jaxrs.model.AuditMessage;
import org.folio.rest.jaxrs.model.Event;
import org.folio.rest.jaxrs.model.MessagingModule;
import org.folio.rest.util.MessagingModuleFilter;
import org.folio.rest.util.OkapiConnectionParams;
import org.folio.rest.util.RestUtil;
import org.folio.services.ConsumerService;
import org.folio.services.SecurityManager;
import org.folio.services.audit.AuditService;
import org.folio.services.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static io.vertx.core.Future.succeededFuture;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.folio.rest.jaxrs.model.MessagingModule.ModuleRole.SUBSCRIBER;
import static org.folio.rest.util.OkapiConnectionParams.USER_ID;
import static org.folio.rest.util.RestUtil.doRequest;
import static org.folio.services.util.AuditUtil.constructJsonAuditMessage;
import static org.folio.services.util.MessagingModulesUtil.filter;

@Component
public class KafkaConsumerServiceImpl implements ConsumerService {

  private static final Logger LOGGER = LogManager.getLogger();

  private Vertx vertx;
  private PubSubKafkaConfig kafkaConfig;
  private Cache cache;
  private AuditService auditService;
  private SecurityManager securityManager;
  private static final int RETRY_NUMBER =
    Integer.parseInt(System.getenv().getOrDefault("pubsub.delivery.retry.number", "5"));
  /**
   * Pause between delivery attempts. Retrying immediately just spends the whole retry budget
   * inside the same outage, so a failing subscriber gets no chance to recover.
   */
  private static final long RETRY_DELAY_MS =
    Long.parseLong(System.getenv().getOrDefault("pubsub.delivery.retry.delay.ms", "1000"));

  public KafkaConsumerServiceImpl(@Autowired Vertx vertx,
                                  @Autowired PubSubKafkaConfig kafkaConfig,
                                  @Autowired SecurityManager securityManager,
                                  @Autowired Cache cache) {
    this.vertx = vertx;
    this.kafkaConfig = kafkaConfig;
    this.cache = cache;
    this.securityManager = securityManager;
    this.auditService = AuditService.createProxy(vertx);
  }

  @Override
  public Future<Void> subscribe(List<String> eventTypes, OkapiConnectionParams params) {
    Set<String> topics = eventTypes.stream()
      .map(eventType -> new PubSubConfig(kafkaConfig.getEnvId(),
        params.getTenantId(), eventType).getTopicName())
      .collect(Collectors.toSet());
    Map<String, String> consumerProps = kafkaConfig.getConsumerProps();

    // update known OkapiConnectionParams
    if (cache.getKnownOkapiParams(params.getTenantId()) == null) {
      cache.setKnownOkapiParams(params.getTenantId(), params);
    }

    List<Future<Void>> futures = topics.stream()
      .filter(topic -> !cache.containsSubscription(topic))
      .map(topic -> {
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, topic);
        return createKafkaConsumer(vertx, consumerProps)
          .handler(getEventReceivedHandler())
          .subscribe(topic)
          .onSuccess(result -> {
            cache.addSubscription(topic);
            LOGGER.info("Subscribed to topic {}", topic);
          })
          .onFailure(e ->
            LOGGER.error("Could not subscribe to some of the topic {}", topic, e));
      })
      .toList();
    return Future.all(futures).mapEmpty();
  }

  protected KafkaConsumer<String, String> createKafkaConsumer(Vertx vertx,
    Map<String, String> consumerProps) {

    return KafkaConsumer.create(vertx, consumerProps);
  }

  private Handler<KafkaConsumerRecord<String, String>> getEventReceivedHandler() {
    return consumerRecord -> {
      try {
        String value = consumerRecord.value();
        Event event = new JsonObject(value).mapTo(Event.class);
        String tenantId = event.getEventMetadata().getTenantId();
        if (StringUtils.isBlank(tenantId)) {
          LOGGER.error("Kafka record does not contain a tenant id. Event ID {}, published by {}",
            event.getId(), event.getEventMetadata().getPublishedBy());
          return;
        }
        LOGGER.info("Received {} event with id '{}'", event.getEventType(), event.getId());
        auditService.saveAuditMessage(constructJsonAuditMessage(event, tenantId, AuditMessage.State.RECEIVED));
        OkapiConnectionParams knownOkapiParams = cache.getKnownOkapiParams(tenantId);
        if (knownOkapiParams == null) {
          LOGGER.error("Could not find OkapiConnectionParams for tenantId={}", tenantId);
          return;
        }
        // The delivery future must not be dropped: everything that can fail before the first POST
        // is reported through it, and a discarded failure leaves no log line and no audit row.
        deliverEvent(event, knownOkapiParams)
          .onFailure(e -> auditDeliveryFailure(event, tenantId, e));
      } catch (Exception e) {
        LOGGER.error("Error reading event value", e);
      }
    };
  }

  private void auditDeliveryFailure(Event event, String tenantId, Throwable cause) {
    String errorMessage = "%s event with id '%s' was not delivered: %s".formatted(
      event.getEventType(), event.getId(), cause.getMessage());
    LOGGER.error(errorMessage, cause);
    auditService.saveAuditMessage(constructJsonAuditMessage(event, tenantId,
      AuditMessage.State.REJECTED, errorMessage));
  }

  protected Future<Void> deliverEvent(Event event, OkapiConnectionParams params) {
    return deliverEvent(event, params, 0);
  }

  /**
   * Deliver an event, retrying the whole attempt if it fails before any subscriber has been
   * contacted. Failures raised after the first POST are retried per subscriber by
   * {@link #retryDelivery}; this covers the earlier steps - obtaining an access token and loading
   * the messaging modules - which previously abandoned the event on the first error.
   */
  private Future<Void> deliverEvent(Event event, OkapiConnectionParams params, int attempt) {
    return dispatchEvent(event, params)
      .recover(cause -> {
        if (attempt >= RETRY_NUMBER) {
          return Future.failedFuture(cause);
        }
        LOGGER.warn("deliverEvent:: Could not start delivering {} event with id '{}', "
            + "retrying in {} ms (attempt {} of {})", event.getEventType(), event.getId(),
          retryDelayMs(), attempt + 1, RETRY_NUMBER, cause);
        return after(retryDelayMs())
          .compose(v -> deliverEvent(event, params, attempt + 1));
      });
  }

  private Future<Void> dispatchEvent(Event event, OkapiConnectionParams params) {
    Map<MessagingModule, AtomicInteger> retry = new ConcurrentHashMap<>();
    return securityManager.getAccessToken(params)
      .onSuccess(params::setToken)
      .compose(ar -> cache.getMessagingModules())
      .map(messagingModules -> filter(messagingModules,
        new MessagingModuleFilter()
          .withTenantId(params.getTenantId())
          .withModuleRole(SUBSCRIBER)
          .withEventType(event.getEventType())))
      .compose(subscribers -> {
        if (isEmpty(subscribers)) {
          String errorMessage = "There is no SUBSCRIBERS registered for event type %s. Event %s will not be delivered".formatted(event.getEventType(), event.getId());
          LOGGER.error(errorMessage);
          auditService.saveAuditMessage(constructJsonAuditMessage(event, params.getTenantId(), AuditMessage.State.REJECTED, errorMessage));
          return succeededFuture();
        }
        List<Future<RestUtil.WrappedResponse>> futureList = new ArrayList<>();
        subscribers
          .forEach(subscriber -> {
            retry.put(subscriber, new AtomicInteger(0));
            LOGGER.info("Start delivering messages to subscriber {}", subscriber.getSubscriberCallback());
            futureList.add(RestUtil.doRequest(params, subscriber.getSubscriberCallback(), HttpMethod.POST, event.getEventPayload())
              .onComplete(getEventDeliveredHandler(event, params.getTenantId(), subscriber, params, retry)));
          });
        // join, not all: a rejection by one subscriber is reported and retried by the delivery
        // handler and must not be reported again as a failure of the whole event, nor cancel the
        // remaining subscribers.
        return Future.join(futureList)
          .otherwiseEmpty()
          .mapEmpty();
      });
  }

  /** Pause between delivery attempts, in milliseconds. Overridable so tests need not sleep. */
  protected long retryDelayMs() {
    return RETRY_DELAY_MS;
  }

  private Future<Void> after(long delayMs) {
    if (delayMs <= 0) {
      return succeededFuture();
    }
    Promise<Void> promise = Promise.promise();
    vertx.setTimer(delayMs, id -> promise.complete());
    return promise.future();
  }

  protected Handler<AsyncResult<RestUtil.WrappedResponse>> getEventDeliveredHandler(Event event, String tenantId, MessagingModule subscriber, OkapiConnectionParams params, Map<MessagingModule, AtomicInteger> retry) {
    retry.get(subscriber).incrementAndGet();
    return ar -> {
      LOGGER.info("Delivering for event with ID {} was complete. Checking for response...", event.getId());
      if (ar.failed()) {
        String errorMessage = "%s event with id '%s' was not delivered to %s".formatted(event.getEventType(), event.getId(), subscriber.getSubscriberCallback());
        LOGGER.error(errorMessage, ar.cause());
        auditService.saveAuditMessage(constructJsonAuditMessage(event, tenantId, AuditMessage.State.REJECTED, errorMessage));
        retryDelivery(event, subscriber, params, retry);
      } else {
        int statusCode = ar.result().getCode();
        if (statusCode != HttpStatus.HTTP_OK.toInt()
          && statusCode != HttpStatus.HTTP_CREATED.toInt()
          && statusCode != HttpStatus.HTTP_NO_CONTENT.toInt()) {

          String error = "Error delivering %s event with id '%s' to %s, response status code is %s, %s".formatted(
            event.getEventType(), event.getId(), subscriber.getSubscriberCallback(), statusCode, ar.result().getResponse().statusMessage());
          LOGGER.error(error);
          auditService.saveAuditMessage(constructJsonAuditMessage(event, tenantId, AuditMessage.State.REJECTED, error));
          if (statusCode >= 400 && statusCode < 500) {
            LOGGER.info("Invalidating token for tenant {}", tenantId);
            securityManager.invalidateToken(tenantId);
            params.getHeaders().remove(USER_ID);
          }
          retryDelivery(event, subscriber, params, retry);
        } else {
          LOGGER.info("Delivered {} event with id '{}' to {}", event.getEventType(), event.getId(), subscriber.getSubscriberCallback());
          auditService.saveAuditMessage(constructJsonAuditMessage(event, tenantId, AuditMessage.State.DELIVERED));
        }
      }
    };
  }

  private void retryDelivery(Event event, MessagingModule subscriber, OkapiConnectionParams params, Map<MessagingModule, AtomicInteger> retry) {
    if (retry.get(subscriber).get() <= RETRY_NUMBER) {
      LOGGER.info("Retry to deliver event {} event with id '{}' to {} in {} ms", event.getEventType(), event.getId(), subscriber.getSubscriberCallback(), retryDelayMs());
      after(retryDelayMs())
        .compose(v -> securityManager.getAccessToken(params))
        .onSuccess(token -> {
          params.setToken(token);
          doRequest(params, subscriber.getSubscriberCallback(), HttpMethod.POST, event.getEventPayload())
            .onComplete(getEventDeliveredHandler(event, params.getTenantId(), subscriber, params, retry));
        })
        // The retry never reached the subscriber - most likely the token could not be renewed.
        // Without this handler the retry chain would end silently.
        .onFailure(cause -> auditDeliveryFailure(event, params.getTenantId(), cause));
    }
  }
}
