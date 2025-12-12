package org.folio.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.Test;

class PubSubKafkaConfigTest {

  @Test
  void shouldReturnProducerProperties() {
    Map<String, String> producerProps = new PubSubKafkaConfig().getProducerProps();

    assertEquals("PLAINTEXT", producerProps.get("security.protocol"));
    assertEquals("TLSv1.2", producerProps.get("ssl.protocol"));
    assertEquals("JKS", producerProps.get("ssl.truststore.type"));
    assertEquals("JKS", producerProps.get("ssl.keystore.type"));
    assertNull(producerProps.get("ssl.keystore.password"));
  }

  @Test
  void shouldReturnConsumerProperties() {
    Map<String, String> consumerProps = new PubSubKafkaConfig().getConsumerProps();

    assertEquals("PLAINTEXT", consumerProps.get("security.protocol"));
    assertEquals("TLSv1.2", consumerProps.get("ssl.protocol"));
    assertEquals("JKS", consumerProps.get("ssl.truststore.type"));
    assertEquals("JKS", consumerProps.get("ssl.keystore.type"));
    assertNull(consumerProps.get("ssl.keystore.password"));
  }

}
