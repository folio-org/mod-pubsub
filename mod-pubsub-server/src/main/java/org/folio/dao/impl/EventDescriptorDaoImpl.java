package org.folio.dao.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import org.folio.dao.EventDescriptorDao;
import org.folio.dao.PostgresClientFactory;
import org.folio.rest.jaxrs.model.EventDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.folio.rest.tools.ClientHelpers.pojo2json;

/**
 * Implementation for the EventDescriptorDao, works with PostgresClient to access data.
 *
 * @see EventDescriptorDao
 */
@Repository
public class EventDescriptorDaoImpl implements EventDescriptorDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(EventDescriptorDaoImpl.class);

  private static final String TABLE_NAME = "event_descriptor";
  private static final String MODULE_SCHEMA = "pubsub_config";
  private static final String GET_ALL_SQL = "SELECT * FROM %s.%s";
  private static final String GET_BY_ID_SQL = "SELECT * FROM %s.%s WHERE id = ?";
  private static final String INSERT_SQL = "INSERT INTO %s.%s (id, descriptor) VALUES (?, ?)";
  private static final String UPDATE_BY_ID_SQL = "UPDATE %s.%s SET descriptor = ? WHERE id = ?";
  private static final String DELETE_BY_ID_SQL = "DELETE FROM %s.%s WHERE id = ?";

  @Autowired
  private PostgresClientFactory pgClientFactory;

  @Override
  public Future<List<EventDescriptor>> getAll() {
    Promise<RowSet<Row>> promise = Promise.promise();
    String preparedQuery = format(GET_ALL_SQL, MODULE_SCHEMA, TABLE_NAME);
    pgClientFactory.getInstance().select(preparedQuery, promise);
    return promise.future().map(this::mapResultSetToEventDescriptorList);
  }

  @Override
  public Future<Optional<EventDescriptor>> getByEventType(String eventType) {
    Promise<RowSet<Row>> promise = Promise.promise();
    try {
      String preparedQuery = format(GET_BY_ID_SQL, MODULE_SCHEMA, TABLE_NAME);
      pgClientFactory.getInstance().select(preparedQuery, Tuple.of(eventType), promise);
    } catch (Exception e) {
      LOGGER.error("Error getting EventDescriptor by event type '{}'", e, eventType);
      promise.fail(e);
    }
    return promise.future().map(resultSet -> resultSet.rowCount() == 0 ? Optional.empty()
      : Optional.of(mapRowJsonToEventDescriptor(resultSet.iterator().next())));
  }

  @Override
  public Future<List<EventDescriptor>> getByEventTypes(List<String> eventTypes) {
    Promise<RowSet<Row>> promise = Promise.promise();
    String query = getQueryByEventTypes(eventTypes);
    String preparedQuery = format(query, MODULE_SCHEMA, TABLE_NAME);
    pgClientFactory.getInstance().select(preparedQuery, promise);
    return promise.future().map(this::mapResultSetToEventDescriptorList);
  }

  private String getQueryByEventTypes(List<String> eventTypes) {
    StringBuilder query = new StringBuilder(GET_ALL_SQL);
    if (!isEmpty(eventTypes)) {
      String conditionByEventTypes = eventTypes.stream()
        .map(eventType -> new StringBuilder("'").append(eventType).append("'"))
        .collect(Collectors.joining(", ", "id IN (", ")"));

      query.append( " WHERE ").append(conditionByEventTypes);
    }
    return query.toString();
  }

  @Override
  public Future<String> save(EventDescriptor eventDescriptor) {
    Promise<RowSet<Row>> promise = Promise.promise();
    try {
      String query = format(INSERT_SQL, MODULE_SCHEMA, TABLE_NAME);
      pgClientFactory.getInstance().execute(query, Tuple.of(eventDescriptor.getEventType(), pojo2json(eventDescriptor)),
        promise);
    } catch (Exception e) {
      LOGGER.error("Error saving EventDescriptor with event type '{}'", e, eventDescriptor.getEventType());
      promise.fail(e);
    }
    return promise.future().map(updateResult -> eventDescriptor.getEventType());
  }

  @Override
  public Future<EventDescriptor> update(EventDescriptor eventDescriptor) {
    Promise<RowSet<Row>> promise = Promise.promise();
    try {
      String query = format(UPDATE_BY_ID_SQL, MODULE_SCHEMA, TABLE_NAME);
      pgClientFactory.getInstance().execute(query, Tuple.of(pojo2json(eventDescriptor), eventDescriptor.getEventType()),
        promise);
    } catch (Exception e) {
      LOGGER.error("Error updating EventDescriptor by event type '{}'", e, eventDescriptor.getEventType());
      promise.fail(e);
    }
    return promise.future().compose(updateResult -> updateResult.rowCount() == 1
      ? Future.succeededFuture(eventDescriptor)
      : Future.failedFuture(new NotFoundException(format("EventDescriptor with event type '%s' was not updated", eventDescriptor.getEventType()))));
  }

  @Override
  public Future<Boolean> delete(String eventType) {
    Promise<RowSet<Row>> promise = Promise.promise();
    try {
      String query = format(DELETE_BY_ID_SQL, MODULE_SCHEMA, TABLE_NAME);
      pgClientFactory.getInstance().execute(query, Tuple.of(eventType), promise);
    } catch (Exception e) {
      LOGGER.error("Error deleting EventDescriptor with event type '{}'", e, eventType);
      promise.fail(e);
    }
    return promise.future().map(updateResult -> updateResult.rowCount() == 1);
  }

  private EventDescriptor mapRowJsonToEventDescriptor(Row rowAsJson) {
    EventDescriptor eventDescriptor = new EventDescriptor();
    eventDescriptor.setEventType(rowAsJson.getString("id"));
    JsonObject descriptorAsJson = new JsonObject(rowAsJson.getString("descriptor"));
    eventDescriptor.setDescription(descriptorAsJson.getString("description"));
    eventDescriptor.setEventTTL(descriptorAsJson.getInteger("eventTTL"));
    eventDescriptor.setSigned(descriptorAsJson.getBoolean("signed"));
    return eventDescriptor;
  }

  private List<EventDescriptor> mapResultSetToEventDescriptorList(RowSet<Row> resultSet) {
    return Stream.generate(resultSet.iterator()::next)
      .limit(resultSet.size())
      .map(this::mapRowJsonToEventDescriptor)
      .collect(Collectors.toList());
  }
}
