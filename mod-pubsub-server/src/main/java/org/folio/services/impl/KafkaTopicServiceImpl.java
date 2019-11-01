package org.folio.services.impl;

import io.vertx.core.Future;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.folio.kafka.PubSubConsumerConfig;
import org.folio.services.KafkaTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KafkaTopicServiceImpl implements KafkaTopicService {

  @Autowired
  private AdminClient kafkaAdminClient;

  @Override
  public Future<Boolean> createTopics(List<String> eventTypes, String tenantId, int numPartitions, int replicationFactor) {
    List<NewTopic> topics = eventTypes.stream()
      .map(eventType -> new NewTopic(new PubSubConsumerConfig(tenantId, eventType).getTopicName(), numPartitions, (short) replicationFactor))
      .collect(Collectors.toList());
    boolean finished = kafkaAdminClient.createTopics(topics).all().isDone();
    return Future.succeededFuture(finished);
  }
}
