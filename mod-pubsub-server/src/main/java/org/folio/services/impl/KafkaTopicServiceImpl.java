package org.folio.services.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.kafka.KafkaConfig;
import org.folio.kafka.PubSubConfig;
import org.folio.kafka.PubSubKafkaTopic;
import org.folio.kafka.services.KafkaAdminClientService;
import org.folio.services.KafkaTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KafkaTopicServiceImpl implements KafkaTopicService {

  private static final Logger LOGGER = LogManager.getLogger();

  private KafkaAdminClient kafkaAdminClient;
  private KafkaConfig kafkaConfig;
  private final KafkaAdminClientService kafkaAdminClientService;

  public KafkaTopicServiceImpl(@Autowired KafkaAdminClient kafkaAdminClient, @Autowired KafkaConfig kafkaConfig) {
    this.kafkaAdminClient = kafkaAdminClient;
    this.kafkaConfig = kafkaConfig;
    this.kafkaAdminClientService = new KafkaAdminClientService(Vertx.currentContext().owner());
  }

  @Override
  public Future<Void> createTopics(List<String> eventTypes, String tenantId) {
//    List<NewTopic> topics = eventTypes.stream()
//      .map(eventType -> new NewTopic(new PubSubConfig(kafkaConfig.getEnvId(), tenantId, eventType).getTopicName(), kafkaConfig.getNumberOfPartitions(), (short) kafkaConfig.getReplicationFactor()))
//      .collect(Collectors.toList());

    LOGGER.info("Building topics for events: {}", eventTypes);

    PubSubKafkaTopic[] topics = eventTypes.stream()
      .map(eventType -> new PubSubKafkaTopic("mod-pubsub", eventType))
      .toArray(PubSubKafkaTopic[]::new);

    LOGGER.info("Topics built, creating");

    return Future.succeededFuture()
      .compose(r -> kafkaAdminClientService.createKafkaTopics(topics, tenantId))
      .onSuccess(r -> LOGGER.info("Created topics: [{}]", StringUtils.join(eventTypes, ",")))
      .onFailure(e -> LOGGER.error("Some of the topics [{}] were not created. Cause: {}",
        StringUtils.join(eventTypes, ","), e.getMessage(), e))
      .recover(e -> Future.succeededFuture());
  }
}
