package org.folio.rest.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.nio.file.Path;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;

/**
 * Test that the shaded fat uber jar works and that the Dockerfile works.
 */
public class PubSubIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(PubSubIT.class);

  private static final Network network = Network.newNetwork();

  private static final DockerImageName POSTGRES_IMAGE_NAME = DockerImageName.parse(
    Objects.toString(System.getenv("TESTCONTAINERS_POSTGRES_IMAGE"), "postgres:16-alpine"));

  @RegisterExtension
  static WireMockExtension wireMock = WireMockExtension.newInstance()
    .options(WireMockConfiguration.wireMockConfig().dynamicPort())
    .build();

  public static final PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>(POSTGRES_IMAGE_NAME)
      .withNetwork(network)
      .withNetworkAliases("postgres")
      .withExposedPorts(5432)
      .withUsername("username")
      .withPassword("password")
      .withDatabaseName("postgres");

  public static final GenericContainer<?> module =
    new GenericContainer<>(
      new ImageFromDockerfile("mod-pubsub").withDockerfile(Path.of("../Dockerfile")))
      .withNetwork(network)
      .withExposedPorts(8081)
      .withAccessToHost(true)
      .withEnv("DB_HOST", "postgres")
      .withEnv("DB_PORT", "5432")
      .withEnv("DB_USERNAME", "username")
      .withEnv("DB_PASSWORD", "password")
      .withEnv("DB_DATABASE", "postgres")
      .withEnv("SYSTEM_USER_NAME", "test_user")
      .withEnv("SYSTEM_USER_PASSWORD", "test_password");

  @BeforeAll
  public static void beforeClass() {
    postgres.start();
    module.start();

    Testcontainers.exposeHostPorts(wireMock.getPort());

    RestAssured.reset();
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    RestAssured.baseURI = "http://" + module.getHost() + ":" + module.getFirstMappedPort();
    RestAssured.requestSpecification = new RequestSpecBuilder()
        .addHeader("X-Okapi-Tenant", "testtenant")
        .addHeader("X-Okapi-Url", "http://host.testcontainers.internal:" + wireMock.getPort())
        .setContentType(ContentType.JSON)
        .build();

    module.followOutput(new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams());
  }

  @Test
  public void health() {
    when().
      get("/admin/health").
    then().
      statusCode(200).
      body(is("\"OK\""));
  }

  private void postTenant(JsonObject body) {
    String location =
        given().
          body(body.encodePrettily()).
        when().
          post("/_/tenant").
        then().
          statusCode(201).
        extract().
          header("Location");

    when().
      get(location + "?wait=30000").
    then().
      statusCode(200).  // getting job record succeeds
      body("complete", is(true)).  // job is complete
      body("error", is(nullValue()));  // job has succeeded without error
  }

  @Test
  public void installAndUpgrade() {
    wireMock.stubFor(get("/users?query=username%3D%3D%22pub-sub%22").willReturn(okJson("{\"users\":[]}")));
    wireMock.stubFor(post("/users").willReturn(created()));
    wireMock.stubFor(post(urlPathEqualTo("/authn/credentials")).willReturn(created()));
    wireMock.stubFor(get(urlPathMatching("/perms/users/.*")).willReturn(notFound()));
    wireMock.stubFor(post("/perms/users").willReturn(created()));

    postTenant(new JsonObject().put("module_to", "mod_pubsub-999999.0.0"));
    // migrate from 0.0.0 to test that migration is idempotent
    postTenant(new JsonObject().put("module_to", "mod_pubsub-999999.0.0").put("module_from", "mod_pubsub-0.0.0"));

    // smoke test
    given().
      body(new JsonObject()
          .put("eventType", "FOLIO_RELEASE_PARTY_ANNOUNCEMENT")
          .put("eventTTL", "1")
          .encodePrettily()).
    when().
      post("/pubsub/event-types").
    then().
      statusCode(201).
      body("eventType", is("FOLIO_RELEASE_PARTY_ANNOUNCEMENT"));
  }

}
