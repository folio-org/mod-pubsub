package org.folio.kafka.cache;

import net.mguenther.kafka.junit.EmbeddedKafkaCluster;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.TopicDescription;
import org.folio.kafka.KafkaConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static net.mguenther.kafka.junit.EmbeddedKafkaCluster.provisionWith;
import static net.mguenther.kafka.junit.EmbeddedKafkaClusterConfig.useDefaults;
import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_CONFIG;
import static org.apache.kafka.common.config.TopicConfig.CLEANUP_POLICY_DELETE;
import static org.apache.kafka.common.config.TopicConfig.RETENTION_MS_CONFIG;
import static org.folio.kafka.cache.KafkaInternalCache.KAFKA_CACHE_RETENTION_MS_DEFAULT;
import static org.junit.Assert.assertEquals;

public class KafkaInternalCacheTest {

  public static final String CACHE_TOPIC = "events_cache";

  @ClassRule
  public static EmbeddedKafkaCluster kafkaCluster = provisionWith(useDefaults());

  private static KafkaConfig kafkaConfig;
  private static AdminClient adminClient;

  @BeforeClass
  public static void setUpClass() {
    String[] hostAndPort = kafkaCluster.getBrokerList().split(":");
    kafkaConfig = KafkaConfig.builder()
      .kafkaHost(hostAndPort[0])
      .kafkaPort(hostAndPort[1])
      .build();

    adminClient = AdminClient.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getKafkaUrl()));
  }

  @Test
  public void shouldCreateCacheTopicOnCacheInit() throws ExecutionException, TimeoutException, InterruptedException {
    // given
    KafkaInternalCache kafkaInternalCache = KafkaInternalCache.builder()
      .kafkaConfig(kafkaConfig)
      .build();

    // when
    kafkaInternalCache.initKafkaCache();

    //then
    kafkaCluster.exists(CACHE_TOPIC);
    Properties topicConfig = kafkaCluster.fetchTopicConfig(CACHE_TOPIC);
    assertEquals(topicConfig.getProperty(CLEANUP_POLICY_CONFIG), CLEANUP_POLICY_DELETE);
    assertEquals(topicConfig.getProperty(RETENTION_MS_CONFIG), KAFKA_CACHE_RETENTION_MS_DEFAULT);

    Map<String, TopicDescription> topicDescriptionMap = adminClient.describeTopics(Collections.singleton(CACHE_TOPIC)).all().get(60, TimeUnit.SECONDS);
    TopicDescription topicDescription = topicDescriptionMap.get(CACHE_TOPIC);
    assertEquals(1, topicDescription.partitions().size());
    assertEquals(1, topicDescription.partitions().get(0).replicas().size());
  }

  @AfterClass
  public static void teardown() {
    adminClient.close();
  }
}
