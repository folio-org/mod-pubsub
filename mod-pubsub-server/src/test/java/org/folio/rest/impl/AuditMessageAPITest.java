package org.folio.rest.impl;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.Every.everyItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpStatus;
import org.folio.dao.AuditMessageDao;
import org.folio.dao.PostgresClientFactory;
import org.folio.dao.impl.AuditMessageDaoImpl;
import org.folio.rest.jaxrs.model.AuditMessage;
import org.folio.rest.jaxrs.model.AuditMessagePayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import io.restassured.RestAssured;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;

public class AuditMessageAPITest extends AbstractRestTest {

  @Spy
  PostgresClientFactory postgresClientFactory = new PostgresClientFactory(Vertx.vertx());

  @InjectMocks
  AuditMessageDao auditMessageDao = new AuditMessageDaoImpl();

  AutoCloseable openMocks;

  @BeforeEach
  public void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    openMocks.close();
  }

  private final String eventId_1 = UUID.randomUUID().toString();
  private final String eventId_2 = UUID.randomUUID().toString();
  private final String correlationId_1 = UUID.randomUUID().toString();
  private final String correlationId_2 = UUID.randomUUID().toString();
  private final String eventType = "RECORD_CREATED";

  @Test
  public void shouldReturnBadRequestOnGetHistoryIfFromAndToDatesAreNotSet() {
    RestAssured.given()
      .spec(spec)
      .when()
      .get(HISTORY_PATH)
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  public void shouldReturnBadRequestOnGetHistoryIfFromDateIsNotSet() {
    RestAssured.given()
      .spec(spec)
      .when()
      .get(HISTORY_PATH + "?endDate=2019-09-25T12:00:00")
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  public void shouldReturnBadRequestOnGetHistoryIfTillDateIsNotSet() {
    RestAssured.given()
      .spec(spec)
      .when()
      .get(HISTORY_PATH + "?startDate=2019-09-20T12:00:00")
      .then()
      .statusCode(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  public void shouldAcceptDateFormat() {
    RestAssured.given()
      .spec(spec)
      .when()
      .get(HISTORY_PATH + "?startDate=2019-09-20&endDate=2019-09-27")
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("totalRecords", is(0))
      .body("auditMessages", empty());
  }

  @Test
  public void shouldAcceptDateTimeFormat() {
    RestAssured.given()
      .spec(spec)
      .when()
      .get(HISTORY_PATH + "?startDate=2019-09-20T12:00:00&endDate=2019-09-27T12:00:00")
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body("totalRecords", is(0))
      .body("auditMessages", empty());
  }

  @Test
  public void shouldReturnNotFoundOnGetAuditMessagePayload() {
    RestAssured.given()
      .spec(spec)
      .when()
      .get(AUDIT_MESSAGES_PAYLOAD_PATH.formatted(UUID.randomUUID().toString()))
      .then()
      .statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  public void shouldReturnAuditMessagePayloadOnGet() {
    //VertxTestContext context = new VertxTestContext();
    addTestData().onComplete(ar -> {
      RestAssured.given()
        .spec(spec)
        .when()
        .get(AUDIT_MESSAGES_PAYLOAD_PATH.formatted(eventId_1))
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("eventId", is(eventId_1));
      //context.completeNow();
    });
  }

  @Test
  public void shouldReturnAuditMessagesOnGetFilteredByDates() {
    VertxTestContext context = new VertxTestContext();
    addTestData().onComplete(ar -> {
      RestAssured.given()
        .spec(spec)
        .when()
        .get(HISTORY_PATH + "?startDate=2019-09-20T12:00:00&endDate=2019-09-24T12:00:00")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("totalRecords", is(2))
        .body("auditMessages.size()", is(2));
      context.completeNow();
    });
  }

  @Test
  public void shouldReturnAuditMessagesOnGetFilteredByEventId() {
    VertxTestContext context = new VertxTestContext();
    addTestData().onComplete(ar -> {
      RestAssured.given()
        .spec(spec)
        .when()
        .get(HISTORY_PATH + "?startDate=2019-09-14T12:00:00&endDate=2019-09-26T12:00:00&eventId=" + eventId_1)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("totalRecords", is(2))
        .body("auditMessages.size()", is(2))
        .body("auditMessages*.eventId", everyItem(is(eventId_1)));
      context.completeNow();
    });
  }

  @Test
  public void shouldReturnAuditMessagesOnGetFilteredByEventType() {
    VertxTestContext context = new VertxTestContext();
    addTestData().onComplete(ar -> {
      RestAssured.given()
        .spec(spec)
        .when()
        .get(HISTORY_PATH + "?startDate=2019-09-14T12:00:00&endDate=2019-09-26T12:00:00&eventType=" + eventType)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("totalRecords", is(3))
        .body("auditMessages.size()", is(3))
        .body("auditMessages*.eventType", everyItem(is(eventType)));
      context.completeNow();
    });
  }

  @Test
  public void shouldReturnAuditMessagesOnGetFilteredByCorrelationId() {
    VertxTestContext context = new VertxTestContext();
    addTestData().onComplete(ar -> {
      RestAssured.given()
        .spec(spec)
        .when()
        .get(HISTORY_PATH + "?startDate=2019-09-14T12:00:00&endDate=2019-09-26T12:00:00&correlationId=" + correlationId_2)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("totalRecords", is(2))
        .body("auditMessages.size()", is(2))
        .body("auditMessages*.correlationId", everyItem(is(correlationId_2)));
      context.completeNow();
    });
  }

  @Test
  public void shouldReturnAuditMessagesOnGetFilteredOnlyByOneDayWithoutTime() {
    VertxTestContext context = new VertxTestContext();
    addTestData().onComplete(ar -> {
      RestAssured.given()
        .spec(spec)
        .when()
        .get(HISTORY_PATH + "?startDate=2019-09-27&endDate=2019-09-27")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("totalRecords", is(2))
        .body("auditMessages.size()", is(2));
      context.completeNow();
    });
  }

  @Test
  public void shouldReturnAuditMessagesOnGetFilteredByManyDaysWithoutTime() {
    VertxTestContext context = new VertxTestContext();
    addTestData().onComplete(ar -> {
      RestAssured.given()
        .spec(spec)
        .when()
        .get(HISTORY_PATH + "?startDate=2019-09-27&endDate=2019-09-28")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("totalRecords", is(3))
        .body("auditMessages.size()", is(3));
      //context.completeNow();
    });
  }

  @Test
  public void shouldReturnAuditMessagesOnGetFilteredByManyDaysWithEndTime() {
    VertxTestContext context = new VertxTestContext();
    addTestData().onComplete(ar -> {
      RestAssured.given()
        .spec(spec)
        .when()
        .get(HISTORY_PATH + "?startDate=2019-09-27&endDate=2019-09-28T00:00:00")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("totalRecords", is(2))
        .body("auditMessages.size()", is(2));
      context.completeNow();
    });
  }

  private CompositeFuture saveAuditMessages() {
    List<Future<?>> futures = new ArrayList<>();
    String[] dateFormats = {DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()};
    try {
      AuditMessage auditMessage_1 = new AuditMessage()
        .withId(UUID.randomUUID().toString())
        .withEventId(eventId_1)
        .withEventType(eventType)
        .withCorrelationId(correlationId_1)
        .withTenantId(TENANT_ID)
        .withCreatedBy("diku-admin")
        .withPublishedBy("mod-amazing-1.0.0")
        .withAuditDate(DateUtils.parseDate("2019-09-15T13:00:00", dateFormats))
        .withState(AuditMessage.State.CREATED);
      futures.add(auditMessageDao.saveAuditMessage(auditMessage_1));

      AuditMessage auditMessage_2 = new AuditMessage()
        .withId(UUID.randomUUID().toString())
        .withEventId(eventId_1)
        .withEventType("RECORD_UPDATED")
        .withCorrelationId(correlationId_1)
        .withTenantId(TENANT_ID)
        .withCreatedBy("diku-admin")
        .withPublishedBy("mod-fantastic-1.0.0")
        .withAuditDate(DateUtils.parseDate("2019-09-21T13:00:00", dateFormats))
        .withState(AuditMessage.State.PUBLISHED);
      futures.add(auditMessageDao.saveAuditMessage(auditMessage_2));

      AuditMessage auditMessage_3 = new AuditMessage()
        .withId(UUID.randomUUID().toString())
        .withEventId(eventId_2)
        .withEventType(eventType)
        .withCorrelationId(correlationId_2)
        .withTenantId(TENANT_ID)
        .withCreatedBy("diku-admin")
        .withPublishedBy("mod-fabulous-1.0.0")
        .withAuditDate(DateUtils.parseDate("2019-09-22T13:00:00", dateFormats))
        .withState(AuditMessage.State.CREATED);
      futures.add(auditMessageDao.saveAuditMessage(auditMessage_3));

      AuditMessage auditMessage_4 = new AuditMessage()
        .withId(UUID.randomUUID().toString())
        .withEventId(eventId_2)
        .withEventType(eventType)
        .withCorrelationId(correlationId_2)
        .withTenantId(TENANT_ID)
        .withCreatedBy("diku-admin")
        .withPublishedBy("mod-perfect-2.0.0")
        .withAuditDate(DateUtils.parseDate("2019-09-25T13:00:00", dateFormats))
        .withState(AuditMessage.State.RECEIVED);
      futures.add(auditMessageDao.saveAuditMessage(auditMessage_4));

      AuditMessage auditMessage_5 = new AuditMessage()
        .withId(UUID.randomUUID().toString())
        .withEventId(eventId_2)
        .withEventType(eventType)
        .withCorrelationId(correlationId_2)
        .withTenantId(TENANT_ID)
        .withCreatedBy("diku-admin")
        .withPublishedBy("mod-perfect-2.0.0")
        .withAuditDate(DateUtils.parseDate("2019-09-27T11:00:00", dateFormats))
        .withState(AuditMessage.State.RECEIVED);
      futures.add(auditMessageDao.saveAuditMessage(auditMessage_5));

      AuditMessage auditMessage_6 = new AuditMessage()
        .withId(UUID.randomUUID().toString())
        .withEventId(eventId_2)
        .withEventType(eventType)
        .withCorrelationId(correlationId_2)
        .withTenantId(TENANT_ID)
        .withCreatedBy("diku-admin")
        .withPublishedBy("mod-perfect-2.0.0")
        .withAuditDate(DateUtils.parseDate("2019-09-27T22:00:00", dateFormats))
        .withState(AuditMessage.State.RECEIVED);
      futures.add(auditMessageDao.saveAuditMessage(auditMessage_6));

      AuditMessage auditMessage_7 = new AuditMessage()
        .withId(UUID.randomUUID().toString())
        .withEventId(eventId_2)
        .withEventType(eventType)
        .withCorrelationId(correlationId_2)
        .withTenantId(TENANT_ID)
        .withCreatedBy("diku-admin")
        .withPublishedBy("mod-perfect-2.0.0")
        .withAuditDate(DateUtils.parseDate("2019-09-28T20:00:00", dateFormats))
        .withState(AuditMessage.State.RECEIVED);
      futures.add(auditMessageDao.saveAuditMessage(auditMessage_7));
    } catch (Exception e) {
      futures.add(Future.failedFuture(e));
    }
    return Future.all(futures);
  }

  private Future<AuditMessagePayload> saveAuditMessagePayloads() {
    AuditMessagePayload auditMessagePayload_1 = new AuditMessagePayload()
      .withEventId(eventId_1)
      .withContent(new JsonObject().put("content", "interesting").encode());
    AuditMessagePayload auditMessagePayload_2 = new AuditMessagePayload()
      .withEventId(eventId_2)
      .withContent(new JsonObject().put("content", "not that much").encode());
    return auditMessageDao.saveAuditMessagePayload(auditMessagePayload_1, TENANT_ID)
      .compose(ar -> auditMessageDao.saveAuditMessagePayload(auditMessagePayload_2, TENANT_ID));
  }

  private Future<?> addTestData() {
    return Future.succeededFuture()
      .compose(ar -> saveAuditMessagePayloads())
      .compose(ar -> saveAuditMessages());
  }

}
