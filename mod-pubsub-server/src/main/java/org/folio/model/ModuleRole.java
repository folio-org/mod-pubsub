package org.folio.model;

public enum ModuleRole {

  PUBLISHER("PUBLISHER"),
  SUBSCRIBER("SUBSCRIBER");

  private final String value;

  ModuleRole(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
