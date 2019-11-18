package org.folio.util.pubsub;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.folio.rest.jaxrs.model.Event;
import org.folio.util.pubsub.support.OkapiConnectionParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PubSubClientTest extends AbstractRestTest {

  private static OkapiConnectionParams params = new OkapiConnectionParams();

  @Before
  public void prepareParams() {
    params.setToken(TOKEN);
    params.setOkapiUrl(okapiUrl);
    params.setTenantId(TENANT_ID);
  }

  @Test
  public void RegisterModuleSuccessfully(TestContext context) throws Exception {
    context.assertTrue(PubSubClientUtils.registerModule(params).get());
  }

}
