package org.folio.kafka;

import org.junit.After;
import org.junit.Test;

import java.util.function.Supplier;

import static org.junit.Assert.*;

public class PubSubConfigTest {
  private Supplier<PubSubConfig> pubSubConfigSupplier = () -> new PubSubConfig("env", "tenant", "eventType");

  @Test
  public void checkTopicNameWhenTenantCollectionEnabled() {
    PubSubConfig pubSubConfig = pubSubConfigSupplier.get();
    assertTrue(pubSubConfig.getTopicName().contains("env.pub-sub.tenant.eventType"));

    PubSubConfig.setTenantCollectionTopicsQualifier("ALL");
    pubSubConfig = pubSubConfigSupplier.get();
    assertTrue(pubSubConfig.getTopicName().contains("env.pub-sub.ALL.eventType"));
  }

  @After
  public void after() {
    PubSubConfig.setTenantCollectionTopicsQualifier(null);
  }
}
