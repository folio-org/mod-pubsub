package org.folio.dao;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import javassist.NotFoundException;
import org.folio.model.MessagingModule;
import org.folio.model.ModuleRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Implementation for the MessagingModuleDao, works with PostgresClient to access data.
 *
 * @see MessagingModuleDao
 */
@Repository
public class MessagingModuleDaoImpl implements MessagingModuleDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessagingModuleDaoImpl.class);

  private static final String TABLE_NAME = "messaging_module";
  private static final String MODULE_SCHEMA = "pubsub_config";
  private static final String GET_ALL_SQL = "SELECT * FROM %s.%s ";
  private static final String GET_BY_ID_SQL = "SELECT * FROM %s.%s WHERE id = ?";
  private static final String INSERT_SQL = "INSERT INTO %s.%s (event_type_id, module_id, tenant_id, role, is_applied, subscriber_callback) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
  private static final String UPDATE_BY_ID_SQL = "UPDATE %s.%s SET event_type_id = ?, module_id = ?, tenant_id = ?, role = ?, is_applied = ?, subscriber_callback = ? WHERE id = ?";
  private static final String DELETE_BY_ID_SQL = "DELETE FROM %s.%s WHERE id = ?";

  @Autowired
  private PostgresClientFactory pgClientFactory;

  @Override
  public Future<List<MessagingModule>> getAll() {
    Future<ResultSet> future = Future.future();
    String preparedQuery = format(GET_ALL_SQL, MODULE_SCHEMA, TABLE_NAME);
    pgClientFactory.createInstance().select(preparedQuery, future.completer());
    return future.map(this::mapResultSetToMessagingModuleList);
  }

  @Override
  public Future<Optional<MessagingModule>> getById(long id) {
    Future<ResultSet> future = Future.future();
    String preparedQuery = format(GET_BY_ID_SQL, MODULE_SCHEMA, TABLE_NAME);
    JsonArray params = new JsonArray().add(id);
    pgClientFactory.createInstance().select(preparedQuery, params, future.completer());
    return future.map(resultSet -> resultSet.getResults().isEmpty()
      ? Optional.empty() : Optional.of(mapRowJsonToMessagingModule(resultSet.getRows().get(0))));
  }

  @Override
  public Future<Long> save(MessagingModule messagingModule) {
    Future<JsonArray> future = Future.future();
    try {
      String query = format(INSERT_SQL, MODULE_SCHEMA, TABLE_NAME);
      JsonArray params = new JsonArray()
        .add(messagingModule.getEventType())
        .add(messagingModule.getModuleId())
        .add(messagingModule.getTenantId())
        .add(messagingModule.getModuleRole().value())
        .add(messagingModule.isApplied());
      String subscriberCallback = messagingModule.getSubscriberCallback();
      params.add(subscriberCallback != null ? subscriberCallback : EMPTY);
      pgClientFactory.createInstance().selectSingle(query, params, future.completer());
    } catch (Exception e) {
      LOGGER.error("Error saving MessagingModule", e);
      future.fail(e);
    }
    return future.map(rowAsJsonArray -> rowAsJsonArray.getLong(0));
  }

  @Override
  public Future<MessagingModule> update(long id, MessagingModule messagingModule) {
    Future<UpdateResult> future = Future.future();
    try {
      String query = format(UPDATE_BY_ID_SQL, MODULE_SCHEMA, TABLE_NAME);
      String subscriberCallback = messagingModule.getSubscriberCallback();
      JsonArray params = new JsonArray()
        .add(messagingModule.getEventType())
        .add(messagingModule.getModuleId())
        .add(messagingModule.getTenantId())
        .add(messagingModule.getModuleRole())
        .add(messagingModule.isApplied())
        .add(subscriberCallback != null ? subscriberCallback : EMPTY)
        .add(id);
      pgClientFactory.createInstance().execute(query, params, future.completer());
    } catch (Exception e) {
      LOGGER.error("Error updating MessagingModule by id '{}'", e, id);
      future.fail(e);
    }
    return future.compose(updateResult -> updateResult.getUpdated() == 1
      ? Future.succeededFuture(messagingModule)
      : Future.failedFuture(new NotFoundException(format("MessagingModule by id '%s' was not found", id))));
  }

  @Override
  public Future<Boolean> delete(long id) {
    Future<UpdateResult> future = Future.future();
    String query = format(DELETE_BY_ID_SQL, MODULE_SCHEMA, TABLE_NAME);
    JsonArray params = new JsonArray().add(id);
    pgClientFactory.createInstance().execute(query, params, future.completer());
    return future.map(updateResult -> updateResult.getUpdated() == 1);
  }

  private MessagingModule mapRowJsonToMessagingModule(JsonObject rowAsJson) {
    MessagingModule messagingModule = new MessagingModule();
    messagingModule.setId(rowAsJson.getLong("id"));
    messagingModule.setEventType(rowAsJson.getString("event_type_id"));
    messagingModule.setModuleId(rowAsJson.getLong("module_id"));
    messagingModule.setTenantId(rowAsJson.getString("tenant_id"));
    messagingModule.setModuleRole(ModuleRole.valueOf(rowAsJson.getString("role")));
    messagingModule.setApplied(rowAsJson.getBoolean("is_applied"));
    messagingModule.setSubscriberCallback(rowAsJson.getString("subscriber_callback"));
    return messagingModule;
  }

  private List<MessagingModule> mapResultSetToMessagingModuleList(ResultSet resultSet) {
    return resultSet.getRows().stream()
      .map(this::mapRowJsonToMessagingModule)
      .collect(Collectors.toList());
  }
}
