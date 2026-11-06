package org.folio.rest.util;

import static io.vertx.core.Future.failedFuture;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.folio.rest.util.OkapiConnectionParams.OKAPI_TENANT_HEADER;
import static org.folio.rest.util.OkapiConnectionParams.OKAPI_TOKEN_HEADER;
import static org.folio.rest.util.OkapiConnectionParams.OKAPI_URL_HEADER;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.PoolOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

/**
 * Util class with static method for sending http request
 */
public final class RestUtil {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * How long a request may go without producing data before it is failed with a
   * {@code TimeoutException}. This is the guard against a peer that accepts a request and then
   * goes quiet; it is applied per request, in milliseconds, so there is no unit ambiguity.
   */
  static final long RESPONSE_TIMEOUT_MS =
    longFromEnv("HTTP_RESPONSE_TIMEOUT_MS", 60_000);

  /**
   * How long a request may wait for a connection from the pool before it is failed. Without it a
   * saturated pool parks requests indefinitely and the caller has nothing to log, audit or retry.
   */
  static final long POOL_WAIT_TIMEOUT_MS =
    longFromEnv("HTTP_POOL_WAIT_TIMEOUT_MS", 10_000);

  /**
   * Idle pooled connections are closed after this many seconds. Vert.x reads
   * {@code setIdleTimeout} in the unit given by {@code setIdleTimeoutUnit} - seconds unless told
   * otherwise - so the unit is always set explicitly alongside it.
   */
  static final int IDLE_TIMEOUT_SECONDS =
    intFromEnv("HTTP_IDLE_TIMEOUT_SECONDS", 60);

  /**
   * Connections per host. The Vert.x default of 5 is far too small for a module whose whole job is
   * fanning events out over HTTP: a handful of slow subscribers is enough to stall every other
   * outbound call, including the system user login.
   */
  static final int MAX_POOL_SIZE =
    intFromEnv("HTTP_MAX_POOL_SIZE", 100);

  /**
   * Requests allowed to queue for a connection. Vert.x defaults to an unbounded queue, which turns
   * a saturated pool into an ever growing backlog; a bounded queue fails fast instead so the
   * failure is logged, audited and retried.
   */
  static final int MAX_WAIT_QUEUE_SIZE =
    intFromEnv("HTTP_MAX_WAIT_QUEUE_SIZE", 1_000);

  public static class WrappedResponse {
    private final int code;
    private final String body;
    private JsonObject json;
    private final HttpResponse<Buffer> response;

    WrappedResponse(int code, String body,
                    HttpResponse<Buffer> response) {
      this.code = code;
      this.body = body;
      this.response = response;
      try {
        json = new JsonObject(body);
      } catch (Exception e) {
        json = null;
      }
    }

    public int getCode() {
      return code;
    }

    public String getBody() {
      return body;
    }

    public HttpResponse<Buffer> getResponse() {
      return response;
    }

    public JsonObject getJson() {
      return json;
    }
  }

  private RestUtil() {
  }

  /**
   * Create http request
   *
   * @param url     - url for http request
   * @param method  - http method
   * @param payload - body of request
   * @return - async http response
   */
  public static <T> Future<WrappedResponse> doRequest(OkapiConnectionParams params, String url,
                                                      HttpMethod method, T payload) {
    try {
      String requestUrl = params.getOkapiUrl() + url;
      WebClient client = getWebClient(params);

      HttpRequest<Buffer> request = client.requestAbs(method, requestUrl)
        // connectTimeout here is the pool-acquisition deadline, not the TCP connect timeout -
        // see HttpRequest#connectTimeout. Both are in milliseconds.
        .connectTimeout(POOL_WAIT_TIMEOUT_MS)
        .idleTimeout(RESPONSE_TIMEOUT_MS);
      buildHeaders(params).forEach(request::putHeader);

      LOGGER.info("Sending {} for {}", method.name(), requestUrl);
      if (method == HttpMethod.PUT || method == HttpMethod.POST) {
        return handleResponse(request.sendBuffer(Buffer.buffer(
          payload instanceof String s ? s : new ObjectMapper().writeValueAsString(payload))));
      } else {
        return handleResponse(request.send());
      }
    } catch (Exception e) {
      LOGGER.error("Error happened during sending request", e);
      return failedFuture(e);
    }
  }

  /**
   * Build the outbound headers for a request. The caller's header map is copied rather than
   * modified in place - {@link OkapiConnectionParams} instances are cached and shared between
   * events, so mutating their headers leaks per-request state across deliveries.
   *
   * @param params - Okapi connection params
   * @return - headers to send, keyed case-insensitively
   */
  private static Map<String, String> buildHeaders(OkapiConnectionParams params) {
    // Okapi header names are case-insensitive, and inbound requests do not agree on a casing.
    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (params.getHeaders() != null) {
      headers.putAll(params.getHeaders());
    }
    headers.put(OKAPI_URL_HEADER, params.getOkapiUrl());
    headers.put(OKAPI_TENANT_HEADER, params.getTenantId());
    headers.put("Content-type", "application/json");
    headers.put("Accept", "application/json, text/plain");

    // Send the token only when there is one. An empty X-Okapi-Token is not equivalent to no
    // token: a gateway or sidecar that would otherwise supply its own credentials sees a present
    // but useless header, and ends up fetching a redundant system user token on every call.
    if (isNotBlank(params.getToken())) {
      headers.put(OKAPI_TOKEN_HEADER, params.getToken());
    } else {
      headers.remove(OKAPI_TOKEN_HEADER);
    }

    // putHeader rejects null values, and a header we have no value for is one we should not send.
    headers.entrySet().removeIf(entry -> entry.getValue() == null);
    return headers;
  }

  private static Future<WrappedResponse> handleResponse(Future<HttpResponse<Buffer>> responseFuture) {
    return responseFuture
      .map(response -> {
        LOGGER.info("Response received with statusCode {}", response.statusCode());
        return new WrappedResponse(response.statusCode(), response.bodyAsString(), response);
      })
      .onFailure(throwable -> LOGGER.error("Error during sending request", throwable));
  }

  /**
   * Cache key for {@link #clients}. The connect timeout is part of the key because it comes from
   * the caller's {@link OkapiConnectionParams}; keying on the Vert.x instance alone would silently
   * apply the first caller's timeout to everybody else.
   */
  private record ClientKey(Vertx vertx, int connectTimeoutMs) {
  }

  private static final Map<ClientKey, WebClient> clients = new ConcurrentHashMap<>();

  /**
   * Prepare WebClient from OkapiConnection params
   *
   * @param params - Okapi connection params
   * @return - Vertx WebClient
   */
  private static WebClient getWebClient(OkapiConnectionParams params) {
    Vertx vertx  =  params.getVertx() != null ? params.getVertx() : Vertx.currentContext().owner();
    return clients.computeIfAbsent(new ClientKey(vertx, params.getTimeout()),
      RestUtil::createWebClient);
  }

  private static WebClient createWebClient(ClientKey key) {
    LOGGER.info("createWebClient:: connectTimeout={}ms, responseTimeout={}ms, "
        + "poolWaitTimeout={}ms, idleTimeout={}s, maxPoolSize={}, maxWaitQueueSize={}",
      key.connectTimeoutMs(), RESPONSE_TIMEOUT_MS, POOL_WAIT_TIMEOUT_MS, IDLE_TIMEOUT_SECONDS,
      MAX_POOL_SIZE, MAX_WAIT_QUEUE_SIZE);
    WebClientOptions options = new WebClientOptions()
      .setConnectTimeout(key.connectTimeoutMs())
      .setIdleTimeout(IDLE_TIMEOUT_SECONDS)
      .setIdleTimeoutUnit(TimeUnit.SECONDS);
    PoolOptions poolOptions = new PoolOptions()
      .setHttp1MaxSize(MAX_POOL_SIZE)
      .setMaxWaitQueueSize(MAX_WAIT_QUEUE_SIZE);
    return WebClient.create(key.vertx(), options, poolOptions);
  }

  private static int intFromEnv(String name, int defaultValue) {
    return (int) longFromEnv(name, defaultValue);
  }

  private static long longFromEnv(String name, long defaultValue) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      return defaultValue;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      LOGGER.warn("longFromEnv:: {} is not a number: '{}', falling back to {}", name, value,
        defaultValue);
      return defaultValue;
    }
  }
}
