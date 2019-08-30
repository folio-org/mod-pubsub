package org.folio.model;

public class MessagingModule {

  private long id;
  private String eventType;
  private long moduleId;
  private String tenantId;
  private ModuleRole moduleRole;
  private boolean applied;
  private String subscriberCallback;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public long getModuleId() {
    return moduleId;
  }

  public void setModuleId(long moduleId) {
    this.moduleId = moduleId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public ModuleRole getModuleRole() {
    return moduleRole;
  }

  public void setModuleRole(ModuleRole moduleRole) {
    this.moduleRole = moduleRole;
  }

  public boolean isApplied() {
    return applied;
  }

  public void setApplied(boolean applied) {
    this.applied = applied;
  }

  public String getSubscriberCallback() {
    return subscriberCallback;
  }

  public void setSubscriberCallback(String subscriberCallback) {
    this.subscriberCallback = subscriberCallback;
  }
}
