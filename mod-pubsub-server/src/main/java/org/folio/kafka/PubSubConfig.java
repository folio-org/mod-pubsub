package org.folio.kafka;

import org.apache.commons.lang3.StringUtils;
import org.folio.rest.tools.utils.ModuleName;

import static java.lang.String.join;

public class PubSubConfig {
  private static final String PUB_SUB_PREFIX = "pub-sub";
  private static final String TENANT_COLLECTION_TOPICS_ENV_VAR_NAME = "KAFKA_PRODUCER_TENANT_COLLECTION";
  private static final String TENANT_COLLECTION_MATCH_REGEX = "[A-Z][A-Z0-9]{0,30}";
  private static String TENANT_COLLECTION_TOPIC_QUALIFIER;
  private static boolean TENANT_COLLECTION_TOPICS_ENABLED;
  private String tenant;
  private String eventType;
  private String groupId;
  private String topicName;

  static {
    TENANT_COLLECTION_TOPIC_QUALIFIER = System.getenv(TENANT_COLLECTION_TOPICS_ENV_VAR_NAME);
    setTenantCollectionTopicsQualifier(TENANT_COLLECTION_TOPIC_QUALIFIER);
  }

  public PubSubConfig(String env, String tenant, String eventType) {
    this.tenant = tenant;
    this.eventType = eventType;
    /* moduleNameWithVersion variable need for unique topic and group names for different pub-sub versions.
    It was encapsulated here, in constructor, for better creating/subscribing/sending events.*/
    String moduleNameWithVersion = ModuleName.getModuleName().replace("_", "-") + "-" + ModuleName.getModuleVersion();
    String topicQualifier = TENANT_COLLECTION_TOPICS_ENABLED ? TENANT_COLLECTION_TOPIC_QUALIFIER : tenant;
    this.groupId = join(".", env, PUB_SUB_PREFIX, topicQualifier, eventType, moduleNameWithVersion);
    this.topicName = join(".", env, PUB_SUB_PREFIX, topicQualifier, eventType, moduleNameWithVersion);
  }

  protected static void setTenantCollectionTopicsQualifier(String value) {
    TENANT_COLLECTION_TOPIC_QUALIFIER = value;
    TENANT_COLLECTION_TOPICS_ENABLED = !StringUtils.isEmpty(TENANT_COLLECTION_TOPIC_QUALIFIER);

    if(TENANT_COLLECTION_TOPICS_ENABLED &&
      !TENANT_COLLECTION_TOPIC_QUALIFIER.matches(TENANT_COLLECTION_MATCH_REGEX)){
      throw new RuntimeException(
        String.format("%s environment variable does not match %s",
          TENANT_COLLECTION_TOPICS_ENV_VAR_NAME,
          TENANT_COLLECTION_MATCH_REGEX));
    }
  }

  public String getTenant() {
    return tenant;
  }

  public String getEventType() {
    return eventType;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getTopicName() {
    return topicName;
  }
}
