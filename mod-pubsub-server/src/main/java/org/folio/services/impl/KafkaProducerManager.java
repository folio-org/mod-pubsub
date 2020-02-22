package org.folio.services.impl;

import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.folio.kafka.KafkaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.folio.rest.RestVerticle.MODULE_SPECIFIC_ARGS;

@Component
public class KafkaProducerManager {

  // number of producers to be created is equal to allocated thread pool
  private static final int NUMBER_OF_PRODUCERS =
    Integer.parseInt(MODULE_SPECIFIC_ARGS.getOrDefault("event.publishing.thread.pool.size", "20"));
  private UnmodifiableList<KafkaProducer<String, String>> producers;

  public KafkaProducerManager(@Autowired Vertx vertx, @Autowired KafkaConfig config) {
    List<KafkaProducer<String, String>> list =
      Stream.generate(() -> KafkaProducer.<String, String>create(vertx, config.getProducerProps()))
        .limit(NUMBER_OF_PRODUCERS)
        .collect(Collectors.toList());
    producers = new UnmodifiableList<>(list);
  }

  public KafkaProducer<String, String> getKafkaProducer() {
    return producers.stream().filter(producer -> !producer.writeQueueFull())
      .findFirst().orElseThrow(() -> new RuntimeException("Kafka Producer write queue is full"));
  }
}
