package org.folio.dao;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import javassist.NotFoundException;
import org.folio.model.EventType;
import org.folio.rest.jaxrs.model.EventDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.folio.rest.persist.PostgresClient.pojo2json;

/**
 * Implementation for the EventTypeDao, works with PostgresClient to access data.
 *
 * @see EventTypeDao
 */
@Repository
public class EventTypeDaoImpl implements EventTypeDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventTypeDaoImpl.class);

  private static final String TABLE_NAME = "event_type";
  private static final String MODULE_SCHEMA = "pubsub_config";
  private static final String GET_ALL_SQL = "SELECT * FROM %s.%s";
  private static final String GET_BY_ID_SQL = "SELECT * FROM %s.%s WHERE id = ?";
  private static final String INSERT_SQL = "INSERT INTO %s.%s (id, descriptor) VALUES (?, ?)";
  private static final String UPDATE_BY_ID_SQL = "UPDATE %s.%s SET descriptor = ? WHERE id = ?";
  private static final String DELETE_BY_ID_SQL = "DELETE FROM %s.%s WHERE id = ?";

  @Autowired
  private PostgresClientFactory pgClientFactory;

  @Override
  public Future<List<EventType>> getAll() {
    Future<ResultSet> future = Future.future();
    String preparedQuery = format(GET_ALL_SQL, MODULE_SCHEMA, TABLE_NAME);
    pgClientFactory.createInstance().select(preparedQuery, future.completer());
    return future.map(this::mapResultSetToEventTypeList);
  }

  @Override
  public Future<Optional<EventType>> getById(String id) {
    Future<ResultSet> future = Future.future();
    try {
      String preparedQuery = format(GET_BY_ID_SQL, MODULE_SCHEMA, TABLE_NAME);
      JsonArray params = new JsonArray().add(id);
      pgClientFactory.createInstance().select(preparedQuery, params, future.completer());
    } catch (Exception e) {
      LOGGER.error("Error getting EntityType by id '{}'", e, id);
      future.fail(e);
    }
    return future.map(resultSet -> resultSet.getResults().isEmpty() ? Optional.empty()
      : Optional.of(mapRowJsonToEventType(resultSet.getRows().get(0))));
  }

  @Override
  public Future<String> save(EventType eventType) {
    Future<UpdateResult> future = Future.future();
    try {
      String query = format(INSERT_SQL, MODULE_SCHEMA, TABLE_NAME);
      JsonArray params = new JsonArray()
        .add(eventType.getId())
        .add(pojo2json(eventType.getEventDescriptor()));
      pgClientFactory.createInstance().execute(query, params, future.completer());
    } catch (Exception e) {
      LOGGER.error("Error saving EntityType with id '{}'", e, eventType.getId());
      future.fail(e);
    }
    return future.map(updateResult -> eventType.getId());
  }

  @Override
  public Future<EventType> update(EventType eventType) {
    Future<UpdateResult> future = Future.future();
    try {
      String query = format(UPDATE_BY_ID_SQL, MODULE_SCHEMA, TABLE_NAME);
      JsonArray params = new JsonArray()
        .add(pojo2json(eventType.getEventDescriptor()))
        .add(eventType.getId());
      pgClientFactory.createInstance().execute(query, params, future.completer());
    } catch (Exception e) {
      LOGGER.error("Error updating EntityType by id '{}'", e, eventType.getId());
      future.fail(e);
    }
    return future.compose(updateResult -> updateResult.getUpdated() == 1 ? Future.succeededFuture(eventType)
      : Future.failedFuture(new NotFoundException(format("EventType by id '%s' was not updated", eventType.getId()))));
  }

  @Override
  public Future<Boolean> delete(String id) {
    Future<UpdateResult> future = Future.future();
    try {
      String query = format(DELETE_BY_ID_SQL, MODULE_SCHEMA, TABLE_NAME);
      JsonArray params = new JsonArray().add(id);
      pgClientFactory.createInstance().execute(query, params, future.completer());
    } catch (Exception e) {
      LOGGER.error("Error deleting EntityType with id '{}'", e, id);
      future.fail(e);
    }
    return future.map(updateResult -> updateResult.getUpdated() == 1);
  }

  private EventType mapRowJsonToEventType(JsonObject rowAsJson) {
    EventType eventType = new EventType();
    eventType.setId(rowAsJson.getString("id"));
    eventType.setEventDescriptor(new JsonObject(rowAsJson.getString("descriptor")).mapTo(EventDescriptor.class));
    return eventType;
  }

  private List<EventType> mapResultSetToEventTypeList(ResultSet resultSet) {
    return resultSet.getRows().stream()
      .map(this::mapRowJsonToEventType)
      .collect(Collectors.toList());
  }
}
