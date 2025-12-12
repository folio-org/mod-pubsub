package org.folio.rest.impl;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.folio.rest.RestVerticle.OKAPI_HEADER_TENANT;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.folio.postgres.testing.PostgresTesterContainer;
import org.folio.rest.RestVerticle;
import org.folio.rest.client.TenantClient;
import org.folio.rest.jaxrs.model.TenantAttributes;
import org.folio.rest.jaxrs.model.TenantJob;
import org.folio.rest.persist.Criteria.Criterion;
import org.folio.rest.persist.PostgresClient;
import org.folio.rest.tools.utils.ModuleName;
import org.folio.rest.tools.utils.NetworkUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.SneakyThrows;

@ExtendWith({VertxExtension.class})
public abstract class AbstractRestTest {
  protected static final String TENANT_ID = "diku";
  private static final String TOKEN = "token";
  private static final String HTTP_PORT = "http.port";
  private static final String DELETE_ALL_SQL = "DELETE FROM pubsub_config.%s";
  private static final String EVENT_DESCRIPTOR_TABLE = "event_descriptor";
  private static final String MESSAGING_MODULE_TABLE = "messaging_module";
  private static final String AUDIT_MESSAGE_PAYLOAD_TABLE = "audit_message_payload";
  private static final String AUDIT_MESSAGE_TABLE = "audit_message";

  protected static final String EVENT_TYPES_PATH = "/pubsub/event-types";
  protected static final String DECLARE_PUBLISHER_PATH = "/declare/publisher";
  protected static final String PUBLISHERS_PATH = "/publishers";
  protected static final String DECLARE_SUBSCRIBER_PATH = "/declare/subscriber";
  protected static final String SUBSCRIBERS_PATH = "/subscribers";
  protected static final String HISTORY_PATH = "pubsub/history";
  protected static final String AUDIT_MESSAGES_PAYLOAD_PATH = "/pubsub/audit-messages/%s/payload";

  private static final String KAFKA_HOST = "KAFKA_HOST";
  private static final String KAFKA_PORT = "KAFKA_PORT";
  private static final String OKAPI_URL_ENV = "OKAPI_URL";

  private static final int PORT = NetworkUtils.nextFreePort();
  protected static final String OKAPI_URL = "http://localhost:" + PORT;

  protected static final String SYSTEM_USER_NAME_ENV = "SYSTEM_USER_NAME";
  protected static final String SYSTEM_USER_PASSWORD_ENV = "SYSTEM_USER_PASSWORD";
  protected static final String SYSTEM_USER_NAME = "test-pubsub-username";
  protected static final String SYSTEM_USER_PASSWORD = "test-pubsub-password";
  protected static final String SYSTEM_USER_TYPE = "system";

  static RequestSpecification spec;
  private static String useExternalDatabase;
  protected static Vertx vertx;
  private static final KafkaContainer kafkaContainer =
    new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"))
      .withStartupAttempts(3);

  @BeforeAll
  public static void setUpClass(final VertxTestContext context) throws Exception {
    vertx = Vertx.vertx();
    runDatabase();
    kafkaContainer.start();

    System.setProperty(KAFKA_HOST, kafkaContainer.getHost());
    System.setProperty(KAFKA_PORT, String.valueOf(kafkaContainer.getFirstMappedPort()));
    System.setProperty(OKAPI_URL_ENV, OKAPI_URL);
    System.setProperty(SYSTEM_USER_NAME_ENV, SYSTEM_USER_NAME);
    System.setProperty(SYSTEM_USER_PASSWORD_ENV, SYSTEM_USER_PASSWORD);

    waitForPostgres();
    waitForKafka();

    deployVerticle()
      .onComplete(context.succeedingThenComplete());
  }

  private static void runDatabase() throws Exception {
    useExternalDatabase = System.getProperty(
      "org.folio.pubsub.test.database",
      "embedded");

    switch (useExternalDatabase) {
      case "environment":
        System.out.println("Using environment settings");
        break;
      case "external":
        String postgresConfigPath = System.getProperty(
          "org.folio.pubsub.test.config",
          "/postgres-conf-local.json");
        PostgresClient.setConfigFilePath(postgresConfigPath);
        break;
      case "embedded":
        var postgresContainer = new PostgresTesterContainer();
        postgresContainer.start("database", "username", "password");
        PostgresClient.setPostgresTester(postgresContainer);
        break;
      default:
        String message = "No understood database choice made." +
          "Please set org.folio.pubsub.test.database" +
          "to 'external', 'environment' or 'embedded'";
        throw new Exception(message);
    }
  }

  @SneakyThrows
  private static Future<Void> deployVerticle() {
    final DeploymentOptions options = new DeploymentOptions()
      .setConfig(new JsonObject()
        .put(HTTP_PORT, PORT)
        .put("spring.configuration", "org.folio.config.TestConfig"));

    TenantClient tenantClient = new TenantClient(OKAPI_URL, TENANT_ID, TOKEN, vertx.createHttpClient());
    TenantAttributes tenantAttributes = new TenantAttributes()
      .withModuleTo(ModuleName.getModuleName() + "-" + ModuleName.getModuleVersion());

    return vertx.deployVerticle(RestVerticle.class.getName(), options)
      .compose(ignored -> tenantClient.postTenant(tenantAttributes))
      .compose(response -> tenantClient.getTenantByOperationId(response.bodyAsJson(TenantJob.class).getId(), 60000))
      .onSuccess(response -> assertTrue(response.bodyAsJson(TenantJob.class).getComplete()))
      .mapEmpty();
  }

  @AfterAll
  @SneakyThrows
  public static void tearDownClass(VertxTestContext context) {
    System.out.println("Tearing down class...");
    vertx.close()
      .onSuccess(res -> {
        if (useExternalDatabase.equals("embedded")) {
          PostgresClient.stopPostgresTester();
        }
        System.clearProperty(KAFKA_HOST);
        System.clearProperty(KAFKA_PORT);
        kafkaContainer.stop();
      })
      .onComplete(context.succeedingThenComplete());
  }

  @BeforeEach
  @SneakyThrows
  public void setUp(VertxTestContext context) {
    spec = new RequestSpecBuilder()
      .setContentType(ContentType.JSON)
      .addHeader(OKAPI_HEADER_TENANT, TENANT_ID)
      .setBaseUri(OKAPI_URL)
      .addHeader("Accept", "text/plain, application/json")
      .build();

    clearModuleSchemaTables()
      .compose(ignored -> clearTenantTables())
      .onComplete(context.succeedingThenComplete());
  }

  private Future<RowSet<Row>> clearModuleSchemaTables() {
    PostgresClient pgClient = PostgresClient.getInstance(vertx);
    return pgClient.execute(DELETE_ALL_SQL.formatted(MESSAGING_MODULE_TABLE), Tuple.tuple())
      .compose(event -> pgClient.execute(DELETE_ALL_SQL.formatted(EVENT_DESCRIPTOR_TABLE), Tuple.tuple()));
  }

  private Future<RowSet<Row>> clearTenantTables() {
    PostgresClient pgClient = PostgresClient.getInstance(vertx, TENANT_ID);
    return pgClient.delete(AUDIT_MESSAGE_TABLE, new Criterion())
      .compose(event -> pgClient.delete(AUDIT_MESSAGE_PAYLOAD_TABLE, new Criterion()));
  }

  private static void waitForPostgres() {
    PostgresClient pgClient = PostgresClient.getInstance(vertx);
    String query = "Select 1";
    AtomicBoolean isReady = new AtomicBoolean();
    await()
      .atMost(120, SECONDS)
      .pollInterval(3, SECONDS)
      .alias("Is Postgres Up?")
      .until(() -> {
        System.out.println("checking to see if postgres is up");

        vertx.runOnContext((at) -> pgClient.select(query)
          .onSuccess(ar -> isReady.set(true)));

        return isReady.get();
      });

    if (!isReady.get())
      throw new RuntimeException("Could not connect to postgres");
  }

  private static void waitForKafka() {
    Supplier<KafkaAdminClient> buildAdminClient = () -> {
      Map<String, String> configs = new HashMap<>();
      configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
      return KafkaAdminClient.create(vertx, configs);
    };
    AtomicBoolean isReady = new AtomicBoolean();

    await()
      .atMost(15, SECONDS)
      .pollDelay(3, SECONDS)
      .pollInterval(3, SECONDS)
      .alias("Is Kafka Up?")
      .until(() -> {
        System.out.println("waitForKafka:: creating client...");
        KafkaAdminClient adminClient = buildAdminClient.get();
        System.out.println("waitForKafka:: listing topics to see if kafka is up...");
        adminClient.listTopics()
          .onComplete(ar -> {
            if (ar.succeeded()) {
              System.out.println("waitForKafka:: Kafka is up");
              isReady.set(true);
            }
            adminClient.close(1000);
          });
        return isReady.get();
      });
  }
}
