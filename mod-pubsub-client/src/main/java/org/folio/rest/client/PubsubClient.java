package org.folio.rest.client;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import org.folio.rest.tools.utils.VertxUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * PubsubClient based on class org.folio.rest.jaxrs.resource.Pubsub
 */
public class PubsubClient {

  private final static String GLOBAL_PATH = "/pubsub";
  private String tenantId;
  private String token;
  private String okapiUrl;
  private HttpClientOptions options;
  private HttpClient httpClient;

  public PubsubClient(String okapiUrl, String tenantId, String token, boolean keepAlive, int connTO, int idleTO) {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    this.tenantId = tenantId;
    this.token = token;
    this.okapiUrl = okapiUrl;
    options = new HttpClientOptions();
    options.setLogActivity(true);
    options.setKeepAlive(keepAlive);
    options.setConnectTimeout(connTO);
    options.setIdleTimeout(idleTO);
    httpClient = VertxUtils.getVertxFromContextOrNew().createHttpClient(options);
  }

  public PubsubClient(String okapiUrl, String tenantId, String token, boolean keepAlive) {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    this(okapiUrl, tenantId, token, keepAlive, 2000, 5000);
  }

  public PubsubClient(String okapiUrl, String tenantId, String token) {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    this(okapiUrl, tenantId, token, true, 2000, 5000);
  }

  /**
   * @deprecated use a constructor that takes a full okapiUrl instead
   */
  @Deprecated
  public PubsubClient(String host, int port, String tenantId, String token, boolean keepAlive, int connTO, int idleTO) {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    this(((("http://" + host) + ":") + port), tenantId, token, keepAlive, connTO, idleTO);
  }

  /**
   * @deprecated use a constructor that takes a full okapiUrl instead
   */
  @Deprecated
  public PubsubClient(String host, int port, String tenantId, String token, boolean keepAlive) {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    this(host, port, tenantId, token, keepAlive, 2000, 5000);
  }

  /**
   * @deprecated use a constructor that takes a full okapiUrl instead
   */
  @Deprecated
  public PubsubClient(String host, int port, String tenantId, String token) {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    this(host, port, tenantId, token, true, 2000, 5000);
  }

  /**
   * Convenience constructor for tests ONLY!<br>Connect to localhost on 8081 as folio_demo tenant.@deprecated  use a constructor that takes a full okapiUrl instead
   */
  @Deprecated
  public PubsubClient() {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    this("localhost", 8081, "folio_demo", "folio_demo", false, 2000, 5000);
  }

  /**
   * Service endpoint "/pubsub/event-types/"+eventTypeName+""+queryParams.toString()
   */
  public void deletePubsubEventTypesByEventTypeName(String eventTypeName, String lang, Handler<HttpClientResponse> responseHandler)
    throws UnsupportedEncodingException {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    if (lang != null) {
      queryParams.append("lang=");
      queryParams.append(URLEncoder.encode(lang, "UTF-8"));
      queryParams.append("&");
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.deleteAbs(okapiUrl + "/pubsub/event-types/" + eventTypeName + "" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Accept", "text/plain");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.end();
  }

  /**
   * Service endpoint "/pubsub/event-types/"+eventTypeName+"/publishers"+queryParams.toString()
   */
  public void deletePubsubEventTypesPublishersByEventTypeName(String eventTypeName, String moduleId, Handler<HttpClientResponse> responseHandler)
    throws UnsupportedEncodingException {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    if (moduleId != null) {
      queryParams.append("moduleId=");
      queryParams.append(URLEncoder.encode(moduleId, "UTF-8"));
      queryParams.append("&");
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.deleteAbs(okapiUrl + "/pubsub/event-types/" + eventTypeName + "/publishers" + queryParams.toString());
    request.handler(responseHandler);
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.end();
  }

  /**
   * Service endpoint "/pubsub/event-types/"+eventTypeName+"/subscribers"+queryParams.toString()
   */
  public void deletePubsubEventTypesSubscribersByEventTypeName(String eventTypeName, String moduleId, Handler<HttpClientResponse> responseHandler)
    throws UnsupportedEncodingException {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    if (moduleId != null) {
      queryParams.append("moduleId=");
      queryParams.append(URLEncoder.encode(moduleId, "UTF-8"));
      queryParams.append("&");
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.deleteAbs(okapiUrl + "/pubsub/event-types/" + eventTypeName + "/subscribers" + queryParams.toString());
    request.handler(responseHandler);
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.end();
  }

  /**
   * Service endpoint "/pubsub/audit-messages/"+eventId+"/payload"+queryParams.toString()
   */
  public void getPubsubAuditMessagesPayloadByEventId(String eventId, Handler<HttpClientResponse> responseHandler) {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    io.vertx.core.http.HttpClientRequest request = httpClient.getAbs(okapiUrl + "/pubsub/audit-messages/" + eventId + "/payload" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Accept", "application/json,text/plain");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.end();
  }

  /**
   * Service endpoint "/pubsub/event-types"+queryParams.toString()
   */
  public void getPubsubEventTypes(String lang, Handler<HttpClientResponse> responseHandler)
    throws UnsupportedEncodingException {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    if (lang != null) {
      queryParams.append("lang=");
      queryParams.append(URLEncoder.encode(lang, "UTF-8"));
      queryParams.append("&");
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.getAbs(okapiUrl + "/pubsub/event-types" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Accept", "application/json,text/plain");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.end();
  }

  /**
   * Service endpoint "/pubsub/event-types/"+eventTypeName+""+queryParams.toString()
   */
  public void getPubsubEventTypesByEventTypeName(String eventTypeName, String lang, Handler<HttpClientResponse> responseHandler)
    throws UnsupportedEncodingException {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    if (lang != null) {
      queryParams.append("lang=");
      queryParams.append(URLEncoder.encode(lang, "UTF-8"));
      queryParams.append("&");
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.getAbs(okapiUrl + "/pubsub/event-types/" + eventTypeName + "" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Accept", "application/json,text/plain");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.end();
  }

  /**
   * Service endpoint "/pubsub/event-types/"+eventTypeName+"/publishers"+queryParams.toString()
   */
  public void getPubsubEventTypesPublishersByEventTypeName(String eventTypeName, Handler<HttpClientResponse> responseHandler) {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    io.vertx.core.http.HttpClientRequest request = httpClient.getAbs(okapiUrl + "/pubsub/event-types/" + eventTypeName + "/publishers" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Accept", "application/json");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.end();
  }

  /**
   * Service endpoint "/pubsub/event-types/"+eventTypeName+"/subscribers"+queryParams.toString()
   */
  public void getPubsubEventTypesSubscribersByEventTypeName(String eventTypeName, Handler<HttpClientResponse> responseHandler) {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    io.vertx.core.http.HttpClientRequest request = httpClient.getAbs(okapiUrl + "/pubsub/event-types/" + eventTypeName + "/subscribers" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Accept", "application/json");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.end();
  }

  /**
   * Service endpoint "/pubsub/history"+queryParams.toString()
   */
  public void getPubsubHistory(String startDate, String endDate, String eventId, String eventType, String correlationId, Handler<HttpClientResponse> responseHandler)
    throws UnsupportedEncodingException {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    if (startDate != null) {
      queryParams.append("startDate=");
      queryParams.append(URLEncoder.encode(startDate, "UTF-8"));
      queryParams.append("&");
    }
    if (endDate != null) {
      queryParams.append("endDate=");
      queryParams.append(URLEncoder.encode(endDate, "UTF-8"));
      queryParams.append("&");
    }
    if (eventId != null) {
      queryParams.append("eventId=");
      queryParams.append(URLEncoder.encode(eventId, "UTF-8"));
      queryParams.append("&");
    }
    if (eventType != null) {
      queryParams.append("eventType=");
      queryParams.append(URLEncoder.encode(eventType, "UTF-8"));
      queryParams.append("&");
    }
    if (correlationId != null) {
      queryParams.append("correlationId=");
      queryParams.append(URLEncoder.encode(correlationId, "UTF-8"));
      queryParams.append("&");
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.getAbs(okapiUrl + "/pubsub/history" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Accept", "application/json,text/plain");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.end();
  }

  /**
   * Service endpoint "/pubsub/event-types"+queryParams.toString()
   */
  public void postPubsubEventTypes(String lang, org.folio.rest.jaxrs.model.EventDescriptor EventDescriptor, Handler<HttpClientResponse> responseHandler)
    throws UnsupportedEncodingException, Exception {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    if (lang != null) {
      queryParams.append("lang=");
      queryParams.append(URLEncoder.encode(lang, "UTF-8"));
      queryParams.append("&");
    }
    Buffer buffer = Buffer.buffer();
    if (EventDescriptor != null) {
      buffer.appendString(org.folio.rest.tools.ClientHelpers.pojo2json(EventDescriptor));
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.postAbs(okapiUrl + "/pubsub/event-types" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Content-type", "application/json");
    request.putHeader("Accept", "application/json,text/plain");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.putHeader("Content-Length", buffer.length() + "");
    request.setChunked(true);
    request.write(buffer);
    request.end();
  }

  /**
   * Service endpoint "/pubsub/event-types/declare/publisher"+queryParams.toString()
   */
  public void postPubsubEventTypesDeclarePublisher(org.folio.rest.jaxrs.model.PublisherDescriptor PublisherDescriptor, Handler<HttpClientResponse> responseHandler)
    throws Exception {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    Buffer buffer = Buffer.buffer();
    if (PublisherDescriptor != null) {
      buffer.appendString(org.folio.rest.tools.ClientHelpers.pojo2json(PublisherDescriptor));
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.postAbs(okapiUrl + "/pubsub/event-types/declare/publisher" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Content-type", "application/json");
    request.putHeader("Accept", "application/json,text/plain");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.putHeader("Content-Length", buffer.length() + "");
    request.setChunked(true);
    request.write(buffer);
    request.end();
  }

  /**
   * Service endpoint "/pubsub/event-types/declare/subscriber"+queryParams.toString()
   */
  public void postPubsubEventTypesDeclareSubscriber(org.folio.rest.jaxrs.model.SubscriberDescriptor SubscriberDescriptor, Handler<HttpClientResponse> responseHandler)
    throws Exception {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    Buffer buffer = Buffer.buffer();
    if (SubscriberDescriptor != null) {
      buffer.appendString(org.folio.rest.tools.ClientHelpers.pojo2json(SubscriberDescriptor));
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.postAbs(okapiUrl + "/pubsub/event-types/declare/subscriber" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Content-type", "application/json");
    request.putHeader("Accept", "application/json,text/plain");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.putHeader("Content-Length", buffer.length() + "");
    request.setChunked(true);
    request.write(buffer);
    request.end();
  }

  /**
   * Service endpoint "/pubsub/publish"+queryParams.toString()
   */
  public void postPubsubPublish(org.folio.rest.jaxrs.model.Event Event, Handler<HttpClientResponse> responseHandler)
    throws Exception {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    Buffer buffer = Buffer.buffer();
    if (Event != null) {
      buffer.appendString(org.folio.rest.tools.ClientHelpers.pojo2json(Event));
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.postAbs(okapiUrl + "/pubsub/publish" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Content-type", "application/json");
    request.putHeader("Accept", "application/json,text/plain");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.putHeader("Content-Length", buffer.length() + "");
    request.setChunked(true);
    request.write(buffer);
    request.end();
  }

  /**
   * Service endpoint "/pubsub/event-types/"+eventTypeName+""+queryParams.toString()
   */
  public void putPubsubEventTypesByEventTypeName(String eventTypeName, String lang, org.folio.rest.jaxrs.model.EventDescriptor EventDescriptor, Handler<HttpClientResponse> responseHandler)
    throws UnsupportedEncodingException, Exception {
    // Auto-generated code
    // - generated by       org.folio.rest.tools.ClientGenerator
    // - generated based on org.folio.rest.jaxrs.resource.PubsubResource
    StringBuilder queryParams = new StringBuilder("?");
    if (lang != null) {
      queryParams.append("lang=");
      queryParams.append(URLEncoder.encode(lang, "UTF-8"));
      queryParams.append("&");
    }
    Buffer buffer = Buffer.buffer();
    if (EventDescriptor != null) {
      buffer.appendString(org.folio.rest.tools.ClientHelpers.pojo2json(EventDescriptor));
    }
    io.vertx.core.http.HttpClientRequest request = httpClient.putAbs(okapiUrl + "/pubsub/event-types/" + eventTypeName + "" + queryParams.toString());
    request.handler(responseHandler);
    request.putHeader("Content-type", "application/json");
    request.putHeader("Accept", "application/json,text/plain");
    if (tenantId != null) {
      request.putHeader("X-Okapi-Token", token);
      request.putHeader("x-okapi-tenant", tenantId);
    }
    if (okapiUrl != null) {
      request.putHeader("X-Okapi-Url", okapiUrl);
    }
    request.putHeader("Content-Length", buffer.length() + "");
    request.setChunked(true);
    request.write(buffer);
    request.end();
  }

  /**
   * Close the client. Closing will close down any pooled connections.
   * Clients should always be closed after use.
   */
  public void close() {
    httpClient.close();
  }

}
