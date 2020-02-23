package org.folio.services.cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.folio.dao.MessagingModuleDao;
import org.folio.rest.jaxrs.model.MessagingModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.IterableUtils.isEmpty;

/**
 * In-memory storage for messaging modules
 */
@Component
public class InternalCache {
  private static final String MESSAGING_MODULES_CACHE_KEY = "messaging_modules";

  private AsyncLoadingCache<String, List<MessagingModule>> cache;
  private MessagingModuleDao messagingModuleDao;

  public InternalCache(@Autowired Vertx vertx, @Autowired MessagingModuleDao messagingModuleDao) {
    this.messagingModuleDao = messagingModuleDao;
    this.cache = Caffeine.newBuilder()
      .executor(serviceExecutor -> vertx.runOnContext(ar -> serviceExecutor.run()))
      .buildAsync(k -> new ArrayList<>());
  }

  public Future<List<MessagingModule>> getMessagingModules() {
    Promise<List<MessagingModule>> promise = Promise.promise();
    cache
      .get(MESSAGING_MODULES_CACHE_KEY)
      .whenComplete((messagingModules, throwable) -> {
        if (throwable == null) {
          if (isEmpty(messagingModules)) {
            messagingModuleDao.getAll()
              .map(messagingModules::addAll)
              .setHandler(ar -> promise.complete(messagingModules));
          } else {
            promise.complete(messagingModules);
          }
        } else {
          promise.fail(throwable);
        }
      });
    return promise.future();
  }

  public void invalidate() {
    cache.synchronous().invalidate(MESSAGING_MODULES_CACHE_KEY);
  }

}
