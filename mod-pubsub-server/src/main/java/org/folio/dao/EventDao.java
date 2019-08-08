package org.folio.dao;

import io.vertx.core.Future;

/**
 * Event data access object
 */
public interface EventDao {
  /**
   * Returns event by
   *
   * @param eventId  event id
   * @param tenantId tenant id
   * @return event entity
   */
  Future getById(String eventId, String tenantId);
}
