package org.folio.services.cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.folio.dao.MessagingModuleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * In-memory storage for messaging modules
 */
@Component
public class MessagingModuleStorage {

  private Cache cache;
  private MessagingModuleDao messagingModuleDao;

  public MessagingModuleStorage(@Autowired Vertx vertx, @Autowired MessagingModuleDao messagingModuleDao) {
    this.cache = new Cache(vertx);
    this.messagingModuleDao = messagingModuleDao;
  }

  public Future<LocalStorage> get(String key) {
    return cache.get(key, this::init);
  }

  private Future<LocalStorage> init(LocalStorage localStorage) {
    return messagingModuleDao.getAll().map(modules ->
      localStorage
        .withInitializedState(true)
        .withMessagingModules(modules));
  }

  public void clearStorage() {
    cache.clear();
  }

  /**
   * In-memory cache to store messaging modules
   */
  private class Cache {
    private AsyncLoadingCache<String, LocalStorage> localCache;

    public Cache(Vertx vertx) {
      localCache = Caffeine.newBuilder()
        .executor(serviceExecutor -> vertx.runOnContext(ar -> serviceExecutor.run()))
        .buildAsync(key -> new LocalStorage().withInitializedState(false));
    }

    /**
     * Provides access to values stored in local cache
     *
     * @param key        key
     * @param initAction action to initialize cache
     * @return storage for the given key
     */
    public Future<LocalStorage> get(String key, Function<LocalStorage, Future<LocalStorage>> initAction) {
      Promise<LocalStorage> promise = Promise.promise();
      localCache.get(key).whenComplete((localStorage, exception) -> {
        if (exception != null) {
          promise.fail(exception);
        } else {
          if (localStorage.isInitialized()) {
            promise.complete(localStorage);
          } else {
            initAction.apply(localStorage).setHandler(ar -> promise.complete(localStorage));
          }
        }
      });
      return promise.future();
    }

    public void clear() {
      localCache.synchronous().invalidateAll();
    }
  }

}
