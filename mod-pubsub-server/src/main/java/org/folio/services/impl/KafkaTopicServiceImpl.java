package org.folio.services.impl;

import static io.vertx.core.Future.succeededFuture;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.kafka.PubSubKafkaTopic;
import org.folio.kafka.services.KafkaAdminClientService;
import org.folio.services.KafkaTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.vertx.core.Future;

@Component
public class KafkaTopicServiceImpl implements KafkaTopicService {

  private static final Logger LOGGER = LogManager.getLogger();

  private final KafkaAdminClientService kafkaAdminClientService;

  public KafkaTopicServiceImpl(@Autowired KafkaAdminClientService kafkaAdminClientService) {
    this.kafkaAdminClientService = kafkaAdminClientService;
  }

  @Override
  public Future<Void> createTopics(List<String> eventTypes, String tenantId) {
    PubSubKafkaTopic[] topics = eventTypes.stream()
      .map(eventType -> new PubSubKafkaTopic("mod-pubsub", eventType))
      .toArray(PubSubKafkaTopic[]::new);

    return succeededFuture()
      .compose(r -> kafkaAdminClientService.createKafkaTopics(topics, tenantId))
      .onSuccess(r -> LOGGER.info("Created topics: [{}]", StringUtils.join(eventTypes, ",")))
      .onFailure(e -> LOGGER.error("Some of the topics [{}] were not created. Cause: {}",
        StringUtils.join(eventTypes, ","), e.getMessage(), e))
      .recover(e -> succeededFuture());
  }
}
