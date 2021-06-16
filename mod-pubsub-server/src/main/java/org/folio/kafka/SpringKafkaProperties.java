package org.folio.kafka;

public final class SpringKafkaProperties {//NOSONAR

  public static final String KAFKA_SECURITY_PROTOCOL = "spring.kafka.security.protocol";

  public static final String KAFKA_SSL_PROTOCOL = "spring.kafka.ssl.protocol";

  public static final String KAFKA_SSL_KEY_PASSWORD = "spring.kafka.ssl.key-password";

  public static final String KAFKA_SSL_TRUSTSTORE_LOCATION = "spring.kafka.ssl.trust-store-location";

  public static final String KAFKA_SSL_TRUSTSTORE_PASSWORD = "spring.kafka.ssl.trust-store-password";

  public static final String KAFKA_SSL_TRUSTSTORE_TYPE = "spring.kafka.ssl.trust-store-type";

  public static final String KAFKA_SSL_KEYSTORE_LOCATION = "spring.kafka.ssl.key-store-location";

  public static final String KAFKA_SSL_KEYSTORE_PASSWORD = "spring.kafka.ssl.key-store-password";

  public static final String KAFKA_SSL_KEYSTORE_TYPE = "spring.kafka.ssl.key-store-type";

  private SpringKafkaProperties() {
    throw new UnsupportedOperationException();
  }
}
