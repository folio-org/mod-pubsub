package org.folio.services;

import io.vertx.core.Future;

import java.util.List;
import java.util.Map;

/**
 * Subscriber interface
 */
public interface ConsumerService {

  /**
   * Creates a consumer and subscribes it to particular topics
   *
   * @param moduleId     module id
   * @param eventTypes   list of event types that specified module is subscribing to receive
   * @param tenantId     tenant id
   * @param okapiHeaders okapi headers
   * @return future with true if succeeded
   */
  Future<Boolean> subscribe(String moduleId, List<String> eventTypes, String tenantId, Map<String, String> okapiHeaders);
}
