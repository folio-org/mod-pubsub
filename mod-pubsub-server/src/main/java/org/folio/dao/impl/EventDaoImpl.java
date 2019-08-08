package org.folio.dao.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import org.folio.dao.EventDao;
import org.folio.dao.PostgresClientFactory;
import org.folio.rest.jaxrs.model.Event;
import org.folio.rest.persist.PostgresClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EventDaoImpl implements EventDao {

  private static final String TABLE_NAME = "events";
  @Autowired
  private PostgresClientFactory pgClientFactory;

  @Override
  public Future<Optional<Event>> getById(String eventId, String tenantId) {
    Future<ResultSet> future = Future.future();
    try {
      String getByIdQuery = "SELECT * FROM %s.%s WHERE id = ?";
      String query = String.format(getByIdQuery, PostgresClient.convertToPsqlStandard(tenantId), TABLE_NAME);
      JsonArray params = new JsonArray().add(eventId);
      pgClientFactory.getInstance(tenantId).select(query, params, future.completer());
    } catch (Exception e) {
      future.fail(e);
    }
    return future.map(resultSet -> {
      if (resultSet.getResults().isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(mapToEvent(resultSet.getRows().get(0)));
      }
    });
  }

  private Event mapToEvent(JsonObject jsonObject) {
    // Override mapping right after Event schema confirmed
    return new Event()
      .withId(jsonObject.getString("id"));
  }
}
