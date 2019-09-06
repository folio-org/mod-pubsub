package org.folio.dao.impl;

import org.folio.rest.jaxrs.model.MessagingModule.ModuleRole;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * Filter for messagingModule entity
 */
public class MessagingModuleFilter {

  private Map<String, String> fieldValuesMap = new HashMap<>();

  public void byEventType(String eventType) {
    fieldValuesMap.put("event_type_id", eventType);
  }

  public void byModuleId(String moduleId) {
    fieldValuesMap.put("module_id", moduleId);
  }

  public void byTenantId(String tenantId) {
    fieldValuesMap.put("tenant_id", tenantId);
  }

  public void byModuleRole(ModuleRole moduleRole) {
    fieldValuesMap.put("role", moduleRole.value());
  }

  public void byApplied(boolean applied) {
    fieldValuesMap.put("is_applied", String.valueOf(applied));
  }

  public void bySubscriberCallback(String subscriberCallback) {
    fieldValuesMap.put("subscriber_callback", subscriberCallback);
  }

  /**
   * Checks whether filter contains selection conditions for messagingModule entity
   *
   * @return true if filter contains conditions, otherwise false
   */
  public boolean isEmpty() {
    return fieldValuesMap.isEmpty();
  }

  /**
   * Returns stream of pairs with column name and selection condition value
   *
   * @return stream of pairs with column name and selection condition value
   */
  public Stream<Entry<String, String>> getFieldValuesStream() {
    return fieldValuesMap.entrySet().stream();

  }
}
