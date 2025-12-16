package org.folio.services.publish;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import org.apache.kafka.common.config.ConfigException;
import org.folio.kafka.PubSubKafkaConfig;
import org.folio.rest.jaxrs.model.Event;
import org.folio.rest.jaxrs.model.EventMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class PublishingServiceImplTest {
  @Test
  void producerConfigException(Vertx vertx, VertxTestContext vtc) {
    var publishingServiceImpl = new PublishingServiceImpl(vertx, mock(PubSubKafkaConfig.class));
    var event = new Event().withEventType("foo")
        .withEventMetadata(new EventMetadata().withCorrelationId("id"));
    publishingServiceImpl.sendEvent(event, null)
    .onComplete(vtc.failing(e -> {
      assertThat(e, is(instanceOf(ConfigException.class)));
      vtc.completeNow();
    }));
  }
}
