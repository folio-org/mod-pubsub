package org.folio.rest.util;

import static io.vertx.core.Future.failedFuture;
import static org.folio.rest.util.OkapiConnectionParams.OKAPI_TENANT_HEADER;
import static org.folio.rest.util.OkapiConnectionParams.OKAPI_TOKEN_HEADER;
import static org.folio.rest.util.OkapiConnectionParams.OKAPI_URL_HEADER;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
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
      Map<String, String> headers = params.getHeaders();
      String requestUrl = params.getOkapiUrl() + url;
      WebClient client = getWebClient(params);

      HttpRequest<Buffer> request = client.requestAbs(method, requestUrl);
      if (headers != null) {
        headers.put(OKAPI_URL_HEADER, params.getOkapiUrl());
        headers.put(OKAPI_TENANT_HEADER, params.getTenantId());
        headers.put(OKAPI_TOKEN_HEADER, params.getToken());
        headers.put("Content-type", "application/json");
        headers.put("Accept", "application/json, text/plain");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
          request.putHeader(entry.getKey(), entry.getValue());
        }
      }
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

  private static Future<WrappedResponse> handleResponse(Future<HttpResponse<Buffer>> responseFuture) {
    return responseFuture
      .map(response -> {
        LOGGER.info("Response received with statusCode {}", response.statusCode());
        return new WrappedResponse(response.statusCode(), response.bodyAsString(), response);
      })
      .onFailure(throwable -> {
        LOGGER.error("Error during sending request", throwable);
      });
  }

  private static final Map<Vertx, WebClient> clients = new HashMap<>();

  /**
   * Prepare WebClient from OkapiConnection params
   *
   * @param params - Okapi connection params
   * @return - Vertx WebClient
   */
  private static synchronized WebClient getWebClient(OkapiConnectionParams params) {
    Vertx vertx  =  params.getVertx() != null ? params.getVertx() : Vertx.currentContext().owner();
    if (clients.containsKey(vertx)) {
      return clients.get(vertx);
    }
    WebClientOptions options = new WebClientOptions();
    options.setConnectTimeout(params.getTimeout());
    options.setIdleTimeout(params.getTimeout());
    WebClient webClient = WebClient.create(vertx, options);
    clients.put(vertx, webClient);
    return webClient;
  }
}
