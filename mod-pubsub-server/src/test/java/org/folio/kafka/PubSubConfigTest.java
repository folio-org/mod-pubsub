package org.folio.kafka;

import org.junit.After;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.*;

public class PubSubConfigTest {
  private final String ENV = "env";
  private final String EVENT_TYPE = "eventType";
  private final String TENANT = "tenant";
  private Supplier<PubSubConfig> pubSubConfigSupplier = () -> new PubSubConfig(ENV, TENANT, EVENT_TYPE);

  @Test
  public void checkTopicNameWhenTenantCollectionEnabled() {
    PubSubConfig pubSubConfig = pubSubConfigSupplier.get();
    assertTrue(pubSubConfig.getTopicName().contains("env.pub-sub.tenant.eventType"));
    assertTrue(pubSubConfig.getTopicName().contains(ENV + ".pub-sub." + TENANT + "." + EVENT_TYPE));
    assertTrue(pubSubConfig.getGroupId().contains(ENV + ".pub-sub." + TENANT + "." + EVENT_TYPE + ".mod-pubsub"));
    assertEquals(pubSubConfig.getTenant(), TENANT);
    assertEquals(pubSubConfig.getEventType(), EVENT_TYPE);

    String qualifier = "ALL";
    PubSubConfig.setTenantCollectionTopicsQualifier(qualifier);
    pubSubConfig = pubSubConfigSupplier.get();
    assertTrue(pubSubConfig.getTopicName().contains(ENV + ".pub-sub." + qualifier + "." + EVENT_TYPE));
    assertTrue(pubSubConfig.getGroupId().contains(ENV + ".pub-sub." + qualifier + "." + EVENT_TYPE + ".mod-pubsub"));
    assertEquals(pubSubConfig.getTenant(), TENANT);
    assertEquals(pubSubConfig.getEventType(), EVENT_TYPE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkQualifierMatchesRegex() {
    PubSubConfig.setTenantCollectionTopicsQualifier("bad_value");
  }

  @After
  public void after() {
    PubSubConfig.setTenantCollectionTopicsQualifier(null);
  }
}
