package org.folio.services;

import io.vertx.core.Context;

/**
 * Startup Service Interface
 */
public interface StartupService {

  /**
   * Initializes all registered active subscribers
   */
  void initSubscribers(Context context);
}
