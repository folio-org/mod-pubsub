package org.folio.dao.util;

import java.util.Date;

public class AuditMessageFilter {

  private Date fromDate;
  private Date tillDate;
  private String eventId;
  private String eventType;
  private String correlationId;

  public Date getFromDate() {
    return fromDate;
  }

  public void setFromDate(Date fromDate) {
    this.fromDate = fromDate;
  }

  public Date getTillDate() {
    return tillDate;
  }

  public void setTillDate(Date tillDate) {
    this.tillDate = tillDate;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public void setCorrelationId(String correlationId) {
    this.correlationId = correlationId;
  }

  public AuditMessageFilter withFrom(Date from) {
    this.fromDate = from;
    return this;
  }

  public AuditMessageFilter withTill(Date till) {
    this.tillDate = till;
    return this;
  }

  public AuditMessageFilter withEventId(String eventId) {
    this.eventId = eventId;
    return this;
  }

  public AuditMessageFilter withEventType(String eventType) {
    this.eventType = eventType;
    return this;
  }

  public AuditMessageFilter withCorrelationId(String correlationId) {
    this.correlationId = correlationId;
    return this;
  }
}
