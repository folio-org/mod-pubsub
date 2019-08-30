package org.folio.dao;

import io.vertx.core.Future;
import org.folio.model.EventType;

import java.util.List;
import java.util.Optional;

/**
 * Event type data access object
 */
public interface EventTypeDao {

  /**
   * Searches for all {@link EventType} in database
   *
   * @return future with EventType list
   */
  Future<List<EventType>> getAll();

  /**
   * Searches {@link EventType} by id
   *
   * @param id eventType id
   * @return future with optional of EventType
   */
  Future<Optional<EventType>> getById(String id);

  /**
   * Saves new {@link EventType} to data base
   *
   * @param eventType eventType entity to save
   * @return eventType id
   */
  Future<String> save(EventType eventType);

  /**
   * Updates {@link EventType} in data base
   *
   *
   * @param eventType entity to update
   * @return future with updated eventType
   */
  Future<EventType> update(EventType eventType);

  /**
   * Deletes {@link EventType} by id
   *
   * @param id eventType id
   * @return future with boolean
   */
  Future<Boolean> delete(String id);
}
