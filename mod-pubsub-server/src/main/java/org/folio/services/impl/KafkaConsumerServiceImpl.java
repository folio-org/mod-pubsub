package org.folio.services.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.folio.HttpStatus;
import org.folio.kafka.KafkaConfig;
import org.folio.kafka.PubSubConfig;
import org.folio.rest.jaxrs.model.AuditMessage;
import org.folio.rest.jaxrs.model.Event;
import org.folio.rest.jaxrs.model.MessagingModule;
import org.folio.rest.util.MessagingModuleFilter;
import org.folio.rest.util.OkapiConnectionParams;
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
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.folio.rest.jaxrs.model.MessagingModule.ModuleRole.SUBSCRIBER;
import static org.folio.rest.util.RestUtil.doRequest;
import static org.folio.services.util.AuditUtil.constructJsonAuditMessage;
import static org.folio.services.util.MessagingModulesUtil.filter;

@Component
public class KafkaConsumerServiceImpl implements ConsumerService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerServiceImpl.class);

  private Vertx vertx;
  private KafkaConfig kafkaConfig;
  private Cache cache;
  private AuditService auditService;
  private SecurityManager securityManager;

  public KafkaConsumerServiceImpl(@Autowired Vertx vertx,
                                  @Autowired KafkaConfig kafkaConfig,
                                  @Autowired SecurityManager securityManager,
                                  @Autowired Cache cache) {
    this.vertx = vertx;
    this.kafkaConfig = kafkaConfig;
    this.cache = cache;
    this.securityManager = securityManager;
    this.auditService = AuditService.createProxy(vertx);
  }

  @Override
  public Future<Boolean> subscribe(String moduleId, List<String> eventTypes, OkapiConnectionParams params) {
    Promise<Boolean> result = Promise.promise();
    Set<String> topics = eventTypes.stream()
      .map(eventType -> new PubSubConfig(params.getTenantId(), eventType).getTopicName())
      .collect(Collectors.toSet());
    Map<String, String> consumerProps = kafkaConfig.getConsumerProps();
    List<Future> list = new ArrayList<>();
    for (String topic : topics) {
      consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, params.getTenantId() + "." + topic);
      Promise<Boolean> promise = Promise.promise();
      KafkaConsumer.<String, String>create(vertx, consumerProps)
        .subscribe(topic, ar -> {
          if (ar.succeeded()) {
            LOGGER.info(format("Subscribed to topic {%s}", topic));
            promise.complete(true);
          } else {
            LOGGER.error(format("Could not subscribe to some of the topic {%s}", topic), ar.cause());
            promise.fail(ar.cause());
          }
        }).handler(getEventReceivedHandler(params));
      list.add(promise.future());
    }
    CompositeFuture.all(list).setHandler(ar -> {
      if (ar.succeeded()) {
        result.complete(true);
      } else {
        result.fail(ar.cause());
      }
    });
    return result.future();
  }

  private Handler<KafkaConsumerRecord<String, String>> getEventReceivedHandler(OkapiConnectionParams params) {
    return record -> {
      try {
        String value = record.value();
        LOGGER.debug("Received event {}", value);
        Event event = new JsonObject(value).mapTo(Event.class);
        auditService.saveAuditMessage(constructJsonAuditMessage(event, params.getTenantId(), AuditMessage.State.RECEIVED));
        deliverEvent(event, params);
      } catch (Exception e) {
        LOGGER.error("Error reading event value", e);
      }
    };
  }

  protected Future<Void> deliverEvent(Event event, OkapiConnectionParams params) {
    return securityManager.getJWTToken(params)
      .onSuccess(params::setToken)
      .compose(ar -> cache.getMessagingModules())
      .map(messagingModules -> filter(messagingModules,
        new MessagingModuleFilter()
          .withTenantId(params.getTenantId())
          .withModuleRole(SUBSCRIBER)
          .withEventType(event.getEventType())))
      .compose(subscribers -> {
        if (isEmpty(subscribers)) {
          String errorMessage = format("There is no SUBSCRIBERS registered for event type %s. Event %s will not be delivered", event.getEventType(), event.getId());
          LOGGER.error(errorMessage);
        } else {
          subscribers
            .forEach(subscriber -> doRequest(event.getEventPayload(), subscriber.getSubscriberCallback(), HttpMethod.POST, params)
              .setHandler(getEventDeliveredHandler(event, params.getTenantId(), subscriber)));
        }
        return Future.succeededFuture();
      });
  }

  protected Handler<AsyncResult<HttpResponse<Buffer>>> getEventDeliveredHandler(Event event, String tenantId, MessagingModule subscriber) {
    return ar -> {
      if (ar.failed()) {
        LOGGER.error("{} event with id '{}' was not delivered to {}", ar.cause(), event.getEventType(), event.getId(), subscriber.getSubscriberCallback());
        auditService.saveAuditMessage(constructJsonAuditMessage(event, tenantId, AuditMessage.State.REJECTED));
      } else if (ar.result().statusCode() != HttpStatus.HTTP_OK.toInt()
        && ar.result().statusCode() != HttpStatus.HTTP_CREATED.toInt()
        && ar.result().statusCode() != HttpStatus.HTTP_NO_CONTENT.toInt()) {
        LOGGER.error("Error delivering {} event with id '{}' to {}, response status code is {}, {}",
          event.getEventType(), event.getId(), subscriber.getSubscriberCallback(), ar.result().statusCode(), ar.result().statusMessage());
        auditService.saveAuditMessage(constructJsonAuditMessage(event, tenantId, AuditMessage.State.REJECTED));
      } else {
        LOGGER.debug("Delivered {} event with id '{}' to {}", event.getEventType(), event.getId(), subscriber.getSubscriberCallback());
        auditService.saveAuditMessage(constructJsonAuditMessage(event, tenantId, AuditMessage.State.DELIVERED));
      }
    };
  }

}
