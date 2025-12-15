package org.folio.dao.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.folio.dao.PostgresClientFactory;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.helpers.LocalRowSet;
import org.folio.rest.util.MessagingModuleFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;

class MessagingModuleDaoImplUnitTest {

  @Mock
  private PostgresClientFactory postgresClientFactory;

  @Mock
  private PostgresClient pgClient;

  @InjectMocks
  private MessagingModuleDaoImpl messagingModuleDao = new MessagingModuleDaoImpl();

  AutoCloseable openMocks;

  @BeforeEach
  void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);
    when(postgresClientFactory.getInstance())
      .thenReturn(pgClient);
  }

  @AfterEach
  void tearDown() throws Exception {
    openMocks.close();
  }


  private void shouldSucceedOnDelete(VertxTestContext context, int rowCount) {
    // given
    when(pgClient.execute(anyString()))
        .thenReturn(Future.succeededFuture(new LocalRowSet(rowCount)));
    // when
    messagingModuleDao.delete(new MessagingModuleFilter())
    // then
    .onComplete(r -> {
      verify(pgClient).execute(anyString());
      context.completeNow();
    });
  }

  @Test
  void shouldSucceedOnDeleteExisting() {
    VertxTestContext context = new VertxTestContext();
    shouldSucceedOnDelete(context, 1);
  }

  @Test
  void shouldSucceedOnDeleteNotFound() {
    VertxTestContext context = new VertxTestContext();
    shouldSucceedOnDelete(context, 0);
  }

}
