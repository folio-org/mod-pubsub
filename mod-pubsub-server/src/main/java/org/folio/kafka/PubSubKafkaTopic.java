package org.folio.kafka;

import org.folio.kafka.services.KafkaTopic;

public record PubSubKafkaTopic(String moduleName, String topicName) implements KafkaTopic {
}
