package org.folio.util.pubsub;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.RegexPattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.http.HttpStatus;
import org.folio.rest.jaxrs.model.Event;
import org.folio.rest.jaxrs.model.EventDescriptor;
import org.folio.rest.jaxrs.model.PublisherDescriptor;
import org.folio.rest.jaxrs.model.SubscriberDescriptor;
import org.folio.rest.jaxrs.model.SubscriptionDefinition;
import org.folio.rest.util.OkapiConnectionParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(VertxUnitRunner.class)
public class PubSubClientTest extends AbstractRestTest {

  private static OkapiConnectionParams params = new OkapiConnectionParams();
  private static OkapiConnectionParams fakeParams = new OkapiConnectionParams();
  private static final EventDescriptor EVENT_DESCRIPTOR = new EventDescriptor()
    .withEventType("record_created")
    .withDescription("Created SRS Marc Bibliographic Record with order data in 9xx fields")
    .withEventTTL(1)
    .withSigned(false);
  private static final JsonObject EVENT = new JsonObject()
    .put("id", UUID.randomUUID().toString())
    .put("eventType", "record_created")
    .put("eventMetadata", new JsonObject()
      .put("tenantId", TENANT_ID)
      .put("eventTTL", 30)
      .put("publishedBy", "mod-very-important-1.0.0"));
  private static final String EVENT_TYPES_PATH = "/pubsub/event-types";
  private static final String DECLARE_PUBLISHER_PATH = "/declare/publisher";
  private static final String DECLARE_SUBSCRIBER_PATH = "/declare/subscriber";
  public static final String MESSAGING_MODULES_PATH = "/pubsub/messaging-modules";

  @Before
  public void prepareParams() {
    params.setToken(TOKEN);
    params.setOkapiUrl(okapiUrl);
    params.setTenantId(TENANT_ID);
    fakeParams.setOkapiUrl(okapiUrlStub);
    fakeParams.setTenantId(TENANT_ID);
    fakeParams.setToken(TOKEN);
  }

  @Test
  public void registerModuleSuccessfully() throws Exception {
    assertTrue(PubSubClientUtils.registerModule(params).get());
  }

  @Test
  public void shouldNotRegisterPublishers() {
    WireMock.stubFor(post("/pubsub/event-types/declare/publisher")
      .willReturn(badRequest()));
    WireMock.stubFor(post("/pubsub/event-types/declare/subscriber")
      .willReturn(badRequest()));
    WireMock.stubFor(post("/pubsub/event-types")
      .willReturn(created()));
    try {
      PubSubClientUtils.registerModule(fakeParams).get();
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  public void shouldNotRegisterSubscribers() {
    WireMock.stubFor(post("/pubsub/event-types/declare/publisher")
      .willReturn(created()));
    WireMock.stubFor(post("/pubsub/event-types")
      .willReturn(created()));
    WireMock.stubFor(post("/pubsub/event-types/declare/subscriber")
      .willReturn(badRequest()));
    try {
      PubSubClientUtils.registerModule(fakeParams).get();
      fail();
    } catch (Exception e) {
      assertTrue(true);
    }
  }

  @Test
  public void shouldPublishEventIfNoSubscribersRegistered() {
    EventDescriptor eventDescriptor = postEventDescriptor();
    registerPublisher(eventDescriptor);
    try {
      Event event = EVENT.mapTo(Event.class);
      assertTrue(PubSubClientUtils.sendEventMessage(event, params).get());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void shouldPublishEvent() {
    EventDescriptor eventDescriptor = postEventDescriptor();
    registerPublisher(eventDescriptor);
    registerSubscriber(eventDescriptor);

    try {
      Event event = EVENT.mapTo(Event.class);
      assertTrue(PubSubClientUtils.sendEventMessage(event, params).get());
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void shouldUnregisterModuleSuccessfully() throws Exception {
    assertTrue(PubSubClientUtils.unregisterModule(params).get());
  }

  @Test(expected = ExecutionException.class)
  public void shouldReturnFailedFutureWhenPubsubReturnsServerError() throws Exception {
    WireMock.stubFor(delete(new UrlPathPattern(new RegexPattern(MESSAGING_MODULES_PATH + "?.*"), true))
      .willReturn(serverError()));
    PubSubClientUtils.unregisterModule(fakeParams).get();
  }

  private void registerPublisher(EventDescriptor eventDescriptor) {
    PublisherDescriptor publisherDescriptor = new PublisherDescriptor()
      .withEventDescriptors(Collections.singletonList(eventDescriptor))
      .withModuleId("mod-very-important-1.0.0");

    Response postResponse = RestAssured.given()
      .spec(spec)
      .body(JsonObject.mapFrom(publisherDescriptor).encode())
      .when()
      .post(EVENT_TYPES_PATH + DECLARE_PUBLISHER_PATH);
    assertThat(postResponse.statusCode(), is(HttpStatus.SC_CREATED));
  }

  private EventDescriptor postEventDescriptor() {
    Response postResponse = RestAssured.given()
      .spec(spec)
      .body(JsonObject.mapFrom(PubSubClientTest.EVENT_DESCRIPTOR).encode())
      .when()
      .post(EVENT_TYPES_PATH);
    assertThat(postResponse.statusCode(), is(HttpStatus.SC_CREATED));
    return new JsonObject(postResponse.body().asString()).mapTo(EventDescriptor.class);
  }

  private void registerSubscriber(EventDescriptor eventDescriptor) {
    SubscriptionDefinition subscriptionDefinition = new SubscriptionDefinition()
      .withEventType(eventDescriptor.getEventType())
      .withCallbackAddress("/call-me-maybe");
    SubscriberDescriptor subscriberDescriptor = new SubscriberDescriptor()
      .withSubscriptionDefinitions(Collections.singletonList(subscriptionDefinition))
      .withModuleId("mod-important-1.0.0");

    Response postResponse = RestAssured.given()
      .spec(spec)
      .body(JsonObject.mapFrom(subscriberDescriptor).encode())
      .when()
      .post(EVENT_TYPES_PATH + DECLARE_SUBSCRIBER_PATH);
    assertThat(postResponse.statusCode(), is(HttpStatus.SC_CREATED));
  }

}
