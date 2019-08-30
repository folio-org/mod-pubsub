package org.folio.model;

import org.folio.rest.jaxrs.model.EventDescriptor;

public class EventType {

  private String id;
  private EventDescriptor eventDescriptor;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public EventDescriptor getEventDescriptor() {
    return eventDescriptor;
  }

  public void setEventDescriptor(EventDescriptor eventDescriptor) {
    this.eventDescriptor = eventDescriptor;
  }
}
