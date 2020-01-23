package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import net.mguenther.kafka.junit.EmbeddedKafkaCluster;
import org.folio.config.ApplicationConfig;
import org.folio.dao.util.LiquibaseUtil;
import org.folio.rest.resource.interfaces.InitAPI;
import org.folio.services.StartupService;
import org.folio.services.audit.AuditService;
import org.folio.spring.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class InitAPIImpl implements InitAPI {

  @Autowired
  private StartupService startupService;

  @Autowired
  private static EmbeddedKafkaCluster embeddedKafkaCluster;

  @Override
  public void init(Vertx vertx, Context context, Handler<AsyncResult<Boolean>> handler) {
    vertx.executeBlocking(
      blockingFuture -> {
        SpringContextUtil.init(vertx, context, ApplicationConfig.class);
        SpringContextUtil.autowireDependencies(this, context);
        LiquibaseUtil.initializeSchemaForModule(vertx);
        boolean isEmbeddedKafka = Boolean.parseBoolean(context.config().getString("embedded_kafka", "false"));
        if(isEmbeddedKafka) {

        }
        startupService.initSubscribers();
        blockingFuture.complete();
      },
      result -> {
        if (result.succeeded()) {
          initAuditService(vertx);
          handler.handle(Future.succeededFuture(true));
        } else {
          handler.handle(Future.failedFuture(result.cause()));
        }
      });
  }

  private void initAuditService(Vertx vertx) {
    new ServiceBinder(vertx)
      .setAddress(AuditService.AUDIT_SERVICE_ADDRESS)
      .register(AuditService.class, AuditService.create());
  }
}
