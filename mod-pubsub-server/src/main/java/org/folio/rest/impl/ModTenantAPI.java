package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static org.folio.rest.jaxrs.resource.Tenant.PostTenantResponse.respond500WithTextPlain;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.folio.liquibase.LiquibaseUtil;
import org.folio.rest.RestVerticle;
import org.folio.rest.annotations.Validate;
import org.folio.rest.jaxrs.model.TenantAttributes;
import org.folio.rest.util.OkapiConnectionParams;
import org.folio.services.SecurityManager;
import org.folio.spring.SpringContextUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import java.util.Map;

public class ModTenantAPI extends TenantAPI {

  @Autowired
  private SecurityManager securityManager;

  public ModTenantAPI() {
    SpringContextUtil.autowireDependencies(this, Vertx.currentContext());
  }

  @Validate
  @Override
  public void postTenant(TenantAttributes tenantAttributes, Map<String, String> headers, Handler<AsyncResult<Response>> handler, Context context) {
    super.postTenant(tenantAttributes, headers, postTenantAr -> {
      if (postTenantAr.failed()) {
        handler.handle(postTenantAr);
      } else {
        String tenantId = headers.get(RestVerticle.OKAPI_HEADER_TENANT);
        Vertx vertx = context.owner();
        vertx.executeBlocking(
          blockingFuture -> {
            LiquibaseUtil.initializeSchemaForTenant(vertx, tenantId);
            OkapiConnectionParams params = new OkapiConnectionParams(headers, vertx);

            securityManager.createPubSubUser(params)
              .compose(ar -> securityManager.loginPubSubUser(params))
              .map(this::toObject)
              .onComplete(blockingFuture);
          },
          result -> handleResult(result, postTenantAr, handler)
        );
      }
    }, context);
  }

  private <T> Object toObject(T specificType) {
    return specificType;
  }

  private void handleResult(AsyncResult<Object> pubSubResult,
    AsyncResult<Response> rmbMigrationResult, Handler<AsyncResult<Response>> handler) {

    if (pubSubResult.succeeded()) {
      handler.handle(rmbMigrationResult);
    } else {
      handler.handle(succeededFuture(respond500WithTextPlain(pubSubResult.cause()
        .getMessage())));
    }
  }
}
