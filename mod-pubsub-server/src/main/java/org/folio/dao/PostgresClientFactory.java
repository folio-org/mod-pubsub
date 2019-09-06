package org.folio.dao;

import io.vertx.core.Vertx;
import org.folio.rest.persist.PostgresClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostgresClientFactory {

  @Autowired
  private Vertx vertx;

  /**
   * Creates instance of Postgres Client
   *
   * @param tenantId tenant id
   * @return Postgres Client
   */
  public PostgresClient getInstance(String tenantId) {
    return PostgresClient.getInstance(vertx, tenantId);
  }

  /**
   * Creates instance of Postgres Client
   *
   * @return Postgres Client
   */
  public PostgresClient getInstance() {
    return PostgresClient.getInstance(vertx);
  }
}
