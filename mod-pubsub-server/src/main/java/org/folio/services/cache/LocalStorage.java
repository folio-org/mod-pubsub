package org.folio.services.cache;

import org.apache.commons.collections4.list.UnmodifiableList;
import org.folio.rest.jaxrs.model.MessagingModule;

import java.util.List;

public class LocalStorage {

  private boolean initializedState;
  private UnmodifiableList<MessagingModule> messagingModules;

  public boolean isInitialized() {
    return initializedState;
  }

  public LocalStorage withInitializedState(boolean initializedState) {
    this.initializedState = initializedState;
    return this;
  }

  public LocalStorage withMessagingModules(List<MessagingModule> messagingModules) {
    this.messagingModules = new UnmodifiableList<>(messagingModules);
    return this;
  }

  public List<MessagingModule> getMessagingModules() {
    return messagingModules;
  }
}
