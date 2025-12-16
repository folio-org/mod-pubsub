package org.folio.services.publish;

import static org.folio.rest.jaxrs.model.AuditMessage.State.PUBLISHED;
import static org.folio.rest.jaxrs.model.AuditMessage.State.REJECTED;
import static org.folio.services.util.AuditUtil.constructJsonAuditMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.kafka.PubSubConfig;
import org.folio.kafka.PubSubKafkaConfig;
import org.folio.rest.jaxrs.model.Event;
import org.folio.services.audit.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.impl.KafkaProducerRecordImpl;

@Component
public class PublishingServiceImpl implements PublishingService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final PubSubKafkaConfig kafkaConfig;
  private final AuditService auditService;
  private final Vertx vertx;

  public PublishingServiceImpl(@Autowired Vertx vertx,
                               @Autowired PubSubKafkaConfig kafkaConfig) {
    this.kafkaConfig = kafkaConfig;
    this.auditService = AuditService.createProxy(vertx);
    this.vertx = vertx;
  }

  @Override
  public Future<Void> sendEvent(Event event, String tenantId) {
    PubSubConfig config = new PubSubConfig(kafkaConfig.getEnvId(), tenantId, event.getEventType());
    KafkaProducerRecord<String, String> kafkaProducerRecord =
        new KafkaProducerRecordImpl<>(config.getTopicName(), Json.encode(event));
    return sendEvent(kafkaProducerRecord, config)
        .onSuccess(x -> {
          LOGGER.info("Sent {} event with id '{}' to topic {}",
              event.getEventType(), event.getId(), config.getTopicName());
          auditService.saveAuditMessage(constructJsonAuditMessage(event, tenantId, PUBLISHED));
        })
        .onFailure(e -> {
          var payload = event.getEventPayload();
          var errorMessage = event.getEventType() + " event to topic " + config.getTopicName() +
              " was not sent. Payload size is " + (payload == null ? 0 : payload.length()) +
              ". " + e.getMessage();
          LOGGER.error("{}", errorMessage, e);
          auditService.saveAuditMessage(constructJsonAuditMessage(event, tenantId, REJECTED, errorMessage));
        });
  }

  private Future<Void> sendEvent(KafkaProducerRecord<String, String> event, PubSubConfig config) {
    try {
      KafkaProducer<String, String> sharedProducer = KafkaProducer.createShared(
          vertx, config.getTopicName() + "_Producer", kafkaConfig.getProducerProps());
      return sharedProducer.write(event)
          .eventually(() -> sharedProducer.close());
    } catch (Exception e) {
      return Future.failedFuture(e);
    }
  }
}
