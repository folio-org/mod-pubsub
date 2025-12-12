package org.folio.dao.impl;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.NotFoundException;

import org.folio.dao.PostgresClientFactory;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.persist.helpers.LocalRowSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.Tuple;

class EventDescriptorDaoImplTest {

  @Mock
  private PostgresClientFactory postgresClientFactory;

  @Mock
  private PostgresClient postgresClient;

  @InjectMocks
  EventDescriptorDaoImpl eventDescriptorDao = new EventDescriptorDaoImpl();

  AutoCloseable openMocks;

  @BeforeEach
  public void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);
    when(postgresClientFactory.getInstance()).thenReturn(postgresClient);
  }

  @AfterEach
  public void tearDown() throws Exception {
    openMocks.close();
  }

  private Future<Void> shouldSucceedOnDelete(VertxTestContext context, int rowCount) {
    // given
    when(postgresClient.execute(anyString(), any(Tuple.class)))
      .thenReturn(Future.succeededFuture(new LocalRowSet(rowCount)));
    // when
    return eventDescriptorDao.delete("event-type")
    // then
    .onComplete(x -> {
        verify(postgresClient).execute(anyString(), any(Tuple.class));
        context.completeNow();
      }
    );
  }

  @Test
  void shouldFailOnDeleteMoreThanOneRow() {
    VertxTestContext context = new VertxTestContext();
    var future = shouldSucceedOnDelete(context, 2);
    assertTrue(future.failed());
    assertInstanceOf(NotFoundException.class, future.cause());
  }

  @Test
  void shouldSucceedOnDeleteExisting() {
    VertxTestContext context = new VertxTestContext();
    shouldSucceedOnDelete(context, 1);
  }

  @Test
  void shouldFailOnDeleteNotFound() {
    VertxTestContext context = new VertxTestContext();
    var future = shouldSucceedOnDelete(context, 0);
    assertTrue(future.failed());
    assertInstanceOf(NotFoundException.class, future.cause());
  }
}
