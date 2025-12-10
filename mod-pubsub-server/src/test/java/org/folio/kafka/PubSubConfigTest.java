package org.folio.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class PubSubConfigTest {
  private static final String ENV = "env";
  private static final String EVENT_TYPE = "eventType";
  private static final String TENANT = "tenant";
  private Supplier<PubSubConfig> pubSubConfigSupplier = () -> new PubSubConfig(ENV, TENANT, EVENT_TYPE);

  @Test
  public void checkTopicNameWhenTenantCollectionEnabled() {
    PubSubConfig pubSubConfig = pubSubConfigSupplier.get();
    assertTrue(pubSubConfig.getTopicName().contains("env.pub-sub.tenant.eventType"));
    assertTrue(pubSubConfig.getTopicName().contains(ENV + ".pub-sub." + TENANT + "." + EVENT_TYPE));
    assertTrue(pubSubConfig.getGroupId().contains(ENV + ".pub-sub." + TENANT + "." + EVENT_TYPE + ".mod-pubsub"));
    assertEquals(TENANT, pubSubConfig.getTenant());
    assertEquals(EVENT_TYPE, pubSubConfig.getEventType());

    String qualifier = "ALL";
    PubSubConfig.setTenantCollectionTopicsQualifier(qualifier);
    pubSubConfig = pubSubConfigSupplier.get();
    assertTrue(pubSubConfig.getTopicName().contains(ENV + ".pub-sub." + qualifier + "." + EVENT_TYPE));
    assertTrue(pubSubConfig.getGroupId().contains(ENV + ".pub-sub." + qualifier + "." + EVENT_TYPE + ".mod-pubsub"));
    assertEquals(TENANT, pubSubConfig.getTenant());
    assertEquals(EVENT_TYPE, pubSubConfig.getEventType());
  }

  @Test
  public void checkQualifierMatchesRegex() {
    assertThrows(IllegalArgumentException.class,
      () -> PubSubConfig.setTenantCollectionTopicsQualifier("bad_value"),
      "Expected setTenantCollectionTopicsQualifier() to throw IllegalArgumentException"
    );
  }

  @AfterEach
  public void after() {
    PubSubConfig.setTenantCollectionTopicsQualifier(null);
  }
}
