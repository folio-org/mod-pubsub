package org.folio.services.impl;

import static org.folio.rest.util.OkapiConnectionParams.OKAPI_TENANT_HEADER;
import static org.folio.rest.util.OkapiConnectionParams.OKAPI_TOKEN_HEADER;
import static org.folio.rest.util.OkapiConnectionParams.OKAPI_URL_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.atLeast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.folio.config.user.SystemUserConfig;
import org.folio.kafka.KafkaConfig;
import org.folio.rest.jaxrs.model.Event;
import org.folio.rest.jaxrs.model.EventMetadata;
import org.folio.rest.jaxrs.model.MessagingModule;
import org.folio.rest.util.OkapiConnectionParams;
import org.folio.rest.util.RestUtil;
import org.folio.services.SecurityManager;
import org.folio.services.cache.Cache;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class ConsumerServiceUnitTest {

  private static final String TENANT = "diku";
  private static final String TOKEN = "token";
  private static final String CALLBACK_ADDRESS = "/source-storage/records";
  private static final String EVENT_TYPE = "record_created";

  private Vertx vertx = Vertx.vertx();
  @Mock
  private KafkaConfig kafkaConfig;
  @Mock
  private Cache cache;
  @Mock
  private SystemUserConfig systemUserConfig;

  @Spy
  @InjectMocks
  private SecurityManager securityManager = Mockito.spy(new SecurityManagerImpl(vertx, cache, systemUserConfig));

  @Spy
  @InjectMocks
  private KafkaConsumerServiceImpl consumerService = new KafkaConsumerServiceImpl(
    vertx, kafkaConfig, securityManager, cache);

  private Map<String, String> headers = new HashMap<>();

  @Rule
  public WireMockRule mockServer = new WireMockRule(
    WireMockConfiguration.wireMockConfig()
      .dynamicPort()
      .notifier(new Slf4jNotifier(true)));

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    doReturn(Future.succeededFuture(TOKEN)).when(securityManager).getJWTToken(any(OkapiConnectionParams.class));

    headers.put(OKAPI_URL_HEADER, "http://localhost:" + mockServer.port());
    headers.put(OKAPI_TENANT_HEADER, TENANT);
    headers.put(OKAPI_TOKEN_HEADER, TOKEN);
  }

  @Test
  public void shouldSendRequestWithoutPayloadToSubscriber(TestContext context) {
    Async async = context.async();

    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS)
      .willReturn(WireMock.ok()));

    var event = buildEvent();
    var params = new OkapiConnectionParams(headers, vertx);

    Future<RestUtil.WrappedResponse> future = RestUtil.doRequest(params, CALLBACK_ADDRESS, HttpMethod.POST, event.getEventPayload());

    future.onComplete(ar -> {
      assertTrue(ar.succeeded());
      List<LoggedRequest> requests = WireMock.findAll(RequestPatternBuilder.allRequests());
      assertEquals(1, requests.size());
      assertEquals(CALLBACK_ADDRESS, requests.get(0).getUrl());
      assertEquals("POST", requests.get(0).getMethod().getName());
      async.complete();
    });
  }

  @Test
  public void shouldSendRequestWithPayloadToSubscriber(TestContext context) {
    Async async = context.async();
    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS)
      .willReturn(WireMock.created()));

    var event = new Event()
      .withId(UUID.randomUUID().toString())
      .withEventType(EVENT_TYPE)
      .withEventMetadata(new EventMetadata()
        .withTenantId(TENANT)
        .withEventTTL(30)
        .withPublishedBy("mod-very-important-1.0.0"))
      .withEventPayload("Very important");
    var params = new OkapiConnectionParams(headers, vertx);
    Future<RestUtil.WrappedResponse> future = RestUtil.doRequest(params, CALLBACK_ADDRESS, HttpMethod.POST, event.getEventPayload());

    future.onComplete(ar -> {
      assertTrue(ar.succeeded());
      List<LoggedRequest> requests = WireMock.findAll(RequestPatternBuilder.allRequests());
      assertEquals(1, requests.size());
      assertEquals(CALLBACK_ADDRESS, requests.get(0).getUrl());
      assertEquals("POST", requests.get(0).getMethod().getName());
      assertEquals(event.getEventPayload(), requests.get(0).getBodyAsString());
      async.complete();
    });
  }

  @Test
  public void shouldNotSendRequestIfNoSubscribersFound(TestContext context) {
    Async async = context.async();
    var event = buildEvent();
    var params = new OkapiConnectionParams(headers, vertx);
    when(cache.getMessagingModules()).thenReturn(Future.succeededFuture(new HashSet<>()));

    Future<Void> future = consumerService.deliverEvent(event, params);

    future.onComplete(ar -> {
      assertTrue(ar.succeeded());
      List<LoggedRequest> requests = WireMock.findAll(RequestPatternBuilder.allRequests());
      assertEquals(0, requests.size());
      async.complete();
    });
  }

  @Test
  public void shouldSendRequestToFoundSubscribers(TestContext context) {
    Async async = context.async();
    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS)
      .willReturn(WireMock.noContent()));

    var event = buildEvent();
    var params = buildOkapiConnectionParams();
    Set<MessagingModule> messagingModuleList = new HashSet<>();
    messagingModuleList.add(new MessagingModule()
      .withId(UUID.randomUUID().toString())
      .withEventType(EVENT_TYPE)
      .withModuleId("mod-source-record-storage-1.0.0")
      .withTenantId(TENANT)
      .withModuleRole(MessagingModule.ModuleRole.SUBSCRIBER)
      .withActivated(true)
      .withSubscriberCallback(CALLBACK_ADDRESS));
    messagingModuleList.add(new MessagingModule()
      .withId(UUID.randomUUID().toString())
      .withEventType(EVENT_TYPE)
      .withModuleId("mod-source-record-manager-1.0.0")
      .withTenantId(TENANT)
      .withModuleRole(MessagingModule.ModuleRole.SUBSCRIBER)
      .withActivated(true)
      .withSubscriberCallback(CALLBACK_ADDRESS));
    when(cache.getMessagingModules()).thenReturn(Future.succeededFuture(messagingModuleList));

    Future<Void> future = consumerService.deliverEvent(event, params);

    future.onComplete(ar -> {
      assertTrue(ar.succeeded());
      verify(consumerService, times(messagingModuleList.size())).getEventDeliveredHandler(any(Event.class), anyString(), any(MessagingModule.class), any(OkapiConnectionParams.class), any(Map.class));
      verify(securityManager, times(0)).invalidateToken(TENANT);
      async.complete();
    });
  }

  @Test
  public void shouldSendRequestAndRetry(TestContext context) {
    Async async = context.async();

    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS)
      .willReturn(WireMock.forbidden()));

    var event = buildEvent();
    var params = buildOkapiConnectionParams();
    Set<MessagingModule> messagingModuleList = new HashSet<>();
    messagingModuleList.add(new MessagingModule()
      .withId(UUID.randomUUID().toString())
      .withEventType(EVENT_TYPE)
      .withModuleId("mod-source-record-storage-1.0.0")
      .withTenantId(TENANT)
      .withModuleRole(MessagingModule.ModuleRole.SUBSCRIBER)
      .withActivated(true)
      .withSubscriberCallback(CALLBACK_ADDRESS));
    messagingModuleList.add(new MessagingModule()
      .withId(UUID.randomUUID().toString())
      .withEventType(EVENT_TYPE)
      .withModuleId("mod-source-record-manager-1.0.0")
      .withTenantId(TENANT)
      .withModuleRole(MessagingModule.ModuleRole.SUBSCRIBER)
      .withActivated(true)
      .withSubscriberCallback(CALLBACK_ADDRESS));
    when(cache.getMessagingModules()).thenReturn(Future.succeededFuture(messagingModuleList));

    Future<Void> future = consumerService.deliverEvent(event, params);

    future.onComplete(ar -> {
      assertTrue(ar.succeeded());
      verify(securityManager, times(messagingModuleList.size())).invalidateToken(TENANT);
      async.complete();
    });
  }

  @Test
  public void shouldInvalidateCacheBeforeRetryIfBadRequest(TestContext context) {
    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS).willReturn(WireMock.badRequest()));
    checkThatInvalidateTokenWasInvoked(context);
  }

  @Test
  public void shouldInvalidateCacheBeforeRetryIfForbidden(TestContext context) {
    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS).willReturn(WireMock.forbidden()));
    checkThatInvalidateTokenWasInvoked(context);
  }

  @Test
  public void shouldInvalidateCacheBeforeRetryIfBadRequestEntity(TestContext context) {
    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS).willReturn(WireMock.badRequestEntity()));
    checkThatInvalidateTokenWasInvoked(context);
  }

  @Test
  public void shouldNotInvalidateCacheIfNoContent(TestContext context) {
    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS).willReturn(WireMock.noContent()));
    checkThatInvalidateTokenWasNotInvoked(context);
  }

  @Test
  public void shouldNotInvalidateCacheIfOk(TestContext context) {
    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS).willReturn(WireMock.ok()));
    checkThatInvalidateTokenWasNotInvoked(context);
  }

  @Test
  public void shouldNotInvalidateCacheIfCreated(TestContext context) {
    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS).willReturn(WireMock.created()));
    checkThatInvalidateTokenWasNotInvoked(context);
  }

  @Test
  public void shouldNotInvalidateCacheBeforeRetryIfServerError(TestContext context) {
    WireMock.stubFor(WireMock.post(CALLBACK_ADDRESS).willReturn(WireMock.serverError()));
    checkThatInvalidateTokenWasNotInvoked(context);
  }

  private void checkThatInvalidateTokenWasInvoked(TestContext context) {
    Async async = context.async();
    var event = buildEvent();
    var params = buildOkapiConnectionParams();
    when(cache.getMessagingModules()).thenReturn(Future.succeededFuture(buildMessagingModules()));

    consumerService.deliverEvent(event, params).onComplete(ar -> {
      assertTrue(ar.succeeded());
      verify(securityManager, atLeast(1)).invalidateToken(TENANT);
      verify(cache, atLeast(1)).invalidateToken(TENANT);
      async.complete();
    });
  }

  private void checkThatInvalidateTokenWasNotInvoked(TestContext context) {
    Async async = context.async();
    var event = buildEvent();
    var params = buildOkapiConnectionParams();
    when(cache.getMessagingModules()).thenReturn(Future.succeededFuture(buildMessagingModules()));

    consumerService.deliverEvent(event, params).onComplete(ar -> {
      assertTrue(ar.succeeded());
      verify(securityManager, never()).invalidateToken(TENANT);
      verify(cache, never()).invalidateToken(TENANT);
      async.complete();
    });
  }

  private Set<MessagingModule> buildMessagingModules() {
    Set<MessagingModule> messagingModules = new HashSet<>();
    messagingModules.add(new MessagingModule()
      .withId(UUID.randomUUID().toString())
      .withEventType(EVENT_TYPE)
      .withModuleId("mod-source-record-storage-1.0.0")
      .withTenantId(TENANT)
      .withModuleRole(MessagingModule.ModuleRole.SUBSCRIBER)
      .withActivated(true)
      .withSubscriberCallback(CALLBACK_ADDRESS));

    return messagingModules;
  }

  private Event buildEvent() {
    return new Event()
      .withId(UUID.randomUUID().toString())
      .withEventType(EVENT_TYPE)
      .withEventMetadata(new EventMetadata()
        .withTenantId(TENANT)
        .withEventTTL(30)
        .withPublishedBy("mod-very-important-1.0.0"));
  }

  @NotNull
  private OkapiConnectionParams buildOkapiConnectionParams() {
    OkapiConnectionParams params = new OkapiConnectionParams(vertx);
    params.setHeaders(headers);
    params.setOkapiUrl(headers.getOrDefault("x-okapi-url", "localhost"));
    params.setTenantId(headers.getOrDefault("x-okapi-tenant", TENANT));
    params.setToken(headers.getOrDefault("x-okapi-token", TOKEN));
    params.setTimeout(2000);

    return params;
  }
}
