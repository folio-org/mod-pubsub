## 2025-03-11 v2.16.0
* [MODPUBSUB-295](https://folio-org.atlassian.net/browse/MODPUBSUB-295) Review and cleanup Module Descriptor
* [MODPUBSUB-302](https://folio-org.atlassian.net/browse/MODPUBSUB-302) Change permissions to support Eureka
* [MODPUBSUB-307](https://folio-org.atlassian.net/browse/MODPUBSUB-307) Add permissions for circulation event handlers
* [MODPUBSUB-309](https://folio-org.atlassian.net/browse/MODPUBSUB-309) Upgrade to kafka-clients 3.9.0, folio-di-support 2.2.0, vertx 4.5.11
* [MODPUBSUB-318](https://folio-org.atlassian.net/browse/MODPUBSUB-318) Upgrade to Java 21

## 2024-11-19 v2.15.1
* [MODPUBSUB-302](https://folio-org.atlassian.net/browse/MODPUBSUB-302) Add permissions in pubsub module for MD file changes in Eureka

## 2024-10-29 v2.15.0
* [MODPUBSUB-301](https://folio-org.atlassian.net/browse/MODPUBSUB-301) Circulation logs are not displayed
* [MODPUBSUB-298](https://folio-org.atlassian.net/browse/MODPUBSUB-298) Upgrade to RMB v35.3.0
* [MODPUBSUB-296](https://folio-org.atlassian.net/browse/MODPUBSUB-296) Add new permission to the list of those granted to the pubsub system user
* [EUREKA-225](https://folio-org.atlassian.net/browse/EUREKA-225) Update module descriptors to use the "metadata" field

## 2024-06-20 v2.14.0
* [MODPUBSUB-290](https://folio-org.atlassian.net/browse/MODPUBSUB-290) Use folio-kafka-wrapper to create topics
* [EUREKA-79](https://folio-org.atlassian.net/browse/EUREKA-79) Skip retrieving token if system user is disabled

## 2024-03-20 v2.13.0
* [MODPUBSUB-291](https://folio-org.atlassian.net/browse/MODPUBSUB-291) Upgrade to RMB 35.2.0, Vertx 4.5.5
* [MODPUBSUB-286](https://folio-org.atlassian.net/browse/MODPUBSUB-286) Fix memory leak issue related to Caffeine usage
* [MODPUBSUB-283](https://folio-org.atlassian.net/browse/MODPUBSUB-283) Upgrade to RMB 35.1.1, folio-di-support 2.0.1, kafka-clients 3.6.0
* [MODPUBSUB-282](https://folio-org.atlassian.net/browse/MODPUBSUB-282) Allow disabling system user
* [MODPUBSUB-278](https://folio-org.atlassian.net/browse/MODPUBSUB-278) Remove default user password

## 2023-10-13 v2.11.0
* [FOLIO-3678](https://issues.folio.org/browse/FOLIO-3678) Use API-related Workflows
* [FOLIO-1021](https://issues.folio.org/browse/FOLIO-1021) Update copyright year
* [MODPUBSUB-264](https://issues.folio.org/browse/MODPUBSUB-264) postgresql 42.5.4, folio-liquibase-util 1.6.0, apk upgrade
* [MODPUBSUB-268](https://issues.folio.org/browse/MODPUBSUB-268) Migrate to Java 17
* [MODPUBSUB-270](https://issues.folio.org/browse/MODPUBSUB-270) Enable tenant collection topics
* [MODPUBSUB-267](https://issues.folio.org/browse/MODPUBSUB-267) Use new RMB read-only APIs
* [MODPUBSUB-272](https://issues.folio.org/browse/MODPUBSUB-272) Refresh token rotation
* [MODPUBSUB-274](https://issues.folio.org/browse/MODPUBSUB-274) Populate pub-sub system user with `system` user type
* [MODPUBSUB-277](https://issues.folio.org/browse/MODPUBSUB-277) Check error in `GET /_/tenant/<tenantid>` in `PubSubIT`
* [MODPUBSUB-279](https://issues.folio.org/browse/MODPUBSUB-279) Upgrade to RMB 35.1.0 and Vertx 4.4.6

## 2023-02-15 v2.9.0
* [MODPUBSUB-245](https://issues.folio.org/browse/MODPUBSUB-245) Use testcontainers kafka instead of kafka-junit

## 2023-02-15 v2.8.0
* [MODPUBSUB-258](https://issues.folio.org/browse/MODPUBSUB-258) Add plugin for publishing javadoc and source code

## 2022-10-18 v2.7.0
* [MODPUBSUB-254](https://issues.folio.org/browse/MODPUBSUB-254) Upgrade to snakeyaml 1.33, Vert.x 4.3.4, RMB 35.0.1
* [MODPUBSUB-248](https://issues.folio.org/browse/MODPUBSUB-248) Remove invalid userId from headers for token invalidation
* [MODPUBSUB-252](https://issues.folio.org/browse/MODPUBSUB-252) Upgrade to RMB 35.0.0
* [MODPUBSUB-251](https://issues.folio.org/browse/MODPUBSUB-251) Upgrade kafka-clients to 3.2.3 to fix out-of-memory vulnerability
* [MODPUBSUB-246](https://issues.folio.org/browse/MODFEE-246) Upgrade dependencies fixing vulnerabilities
* [MODPUBSUB-243](https://issues.folio.org/browse/MODPUBSUB-243) Add cache tenantToken invalidation
* [MODPUBSUB-242](https://issues.folio.org/browse/MODPUBSUB-242) Upgrade lombok to 1.18.24 for Java 17
* [MODPUBSUB-241](https://issues.folio.org/browse/MODPUBSUB-241) Supports users interface versions 15.1 16.0

## 2022-06-29 v2.6.0
* [MODPUBSUB-94](https://issues.folio.org/browse/MODPUBSUB-94) Document how to setup permissions for event callback API
* [MODPUBSUB-91](https://issues.folio.org/browse/MODPUBSUB-91) Document requirements for an event callback API endpoint
* [MODPUBSUB-235](https://issues.folio.org/browse/MODPUBSUB-235) Upgrade to RMB 34.0.0

## 2022-05-04 v2.5.1
* [MODPUBSUB-228](https://issues.folio.org/browse/MODPUBSUB-228) Update kafka-clients fixing timing attack (CVE-2021-38153)
* [MODPUBSUB-229](https://issues.folio.org/browse/MODPUBSUB-229) Remove jaxrs-code-generator (CVE-2019-10172)
* [MODPUBSUB-214](https://issues.folio.org/browse/MODPUBSUB-214) Replace Future<Boolean> by Future<Void> in EventDescriptorDao
* [MODPUBSUB-215](https://issues.folio.org/browse/MODPUBSUB-215) Replace Future<Boolean> by Future<Void> in ConsumerService
* [MODPUBSUB-217](https://issues.folio.org/browse/MODPUBSUB-217) Replace Future<Boolean> by Future<Void> in KafkaTopicService
* [FOLIO-1021](https://issues.folio.org/browse/FOLIO-1021) Update copyright year
* [FOLIO-3231](https://issues.folio.org/browse/FOLIO-3231) Use new api-lint. Use new api-doc. Replace deprecated 'schema' keyword, use 'type' for RAML 1.0.
* [MODPUBSUB-231](https://issues.folio.org/browse/MODPUBSUB-231) Update dependencies fixing CVE-2022-0839, CVE-2020-36518
* [MODPUBSUB-232](https://issues.folio.org/browse/MODPUBSUB-232) jackson-databind 2.13.2.1 (CVE-2020-36518)
* [MODPUBSUB-233](https://issues.folio.org/browse/MODPUBSUB-233) folio-di-support 1.5.1, spring-beans 5.3.19 (CVE-2022-22965)

## 2022-02-22 v2.5.0
* [MODPUBSUB-209](https://issues.folio.org/browse/MODPUBSUB-209) Add pubsub-user permission to post events
* [MODPUBSUB-210](https://issues.folio.org/browse/MODPUBSUB-210) Fix incorrect status for tenant init
* [MODPUBSUB-212](https://issues.folio.org/browse/MODPUBSUB-212) Rely on Future status instead of Boolean in SecurityManager
* [MODPUBSUB-213](https://issues.folio.org/browse/MODPUBSUB-213) Rely on Future status instead of Boolean in MessagingModuleDao
* [MODPUBSUB-221](https://issues.folio.org/browse/MODPUBSUB-221) Fix create/update pubsub user
* [MODPUBSUB-222](https://issues.folio.org/browse/MODPUBSUB-222) Update RMB version to 33.2.4
* [MODPUBSUB-223](https://issues.folio.org/browse/MODPUBSUB-223) Update folio-liquibase-util to v1.3.0
* [MODPUBSUB-202](https://issues.folio.org/browse/MODPUBSUB-202) Bad HTTP client pool utilization in mod-pubsub-client
* Bump postgresql from 42.2.25 to 42.3.3 in /mod-pubsub-server
* OkapiConnectionParams takes vertx parameter
* Extend diagnostics for failed pubsub user creation
* Bump postgresql from 42.2.18 to 42.2.25 in /mod-pubsub-server

## 2022-02-08 v2.4.3
* [MODPUBSUB-224](https://issues.folio.org/browse/MODPUBSUB-224) Update folio-liquibase-util to v1.2.1

## 2021-12-18 v2.4.2
* [MODPUBSUB-204](https://issues.folio.org/browse/MODPUBSUB-204) Upgrade to RMB 33.0.4 and Log4j 2.16.0

## 2021-12-09 v2.4.1
* [MODPUBSUB-201](https://issues.folio.org/browse/MODPUBSUB-201) Socket leak for outgoing HTTP requests

## 2021-09-29 v2.4.0
* [MODPUBSUB-185](https://issues.folio.org/browse/MODPUBSUB-185) Adjust the threshold at which the consumer can be resumed according to the load limit
* [MODPUBSUB-186](https://issues.folio.org/browse/MODPUBSUB-186) Provide 'ssl.endpoint.identification.algorithm' property
* [MODPUBSUB-187](https://issues.folio.org/browse/MODPUBSUB-187) Add support for max.request.size configuration for Kafka messages
* [KAFKAWRAP-2](https://issues.folio.org/browse/KAFKAWRAP-2) Take folio-kafka-wrapper lib out of mod-pubsub repository
* [MODDATAIMP-453](https://issues.folio.org/browse/MODDATAIMP-453) Extends logs   
* [MODPUBSUB-189](https://issues.folio.org/browse/MODPUBSUB-189) Upgrade to RAML Module Builder 33.0.2
* [MODPUBSUB-192](https://issues.folio.org/browse/MODPUBSUB-192) Check if max.request.size is set before putting it as producer props
* [MODPUBSUB-193](https://issues.folio.org/browse/MODPUBSUB-193) Fix tests after RMB update

## 2021-06-17 v2.3.1
* [MODPUBSUB-181](https://issues.folio.org/browse/MODPUBSUB-181) mod-source-record-manager unable to create Kafka topics if tenant ID doesn't match \w{4,}
* [MODPUBSUB-182](https://issues.folio.org/browse/MODPUBSUB-182) Provide properties for Kafka security in mod-pubsub

## 2021-06-10 v2.3.0
* [MODPUBSUB-179](https://issues.folio.org/browse/MODPUBSUB-179) Use local PomReader to retrieve module name and version

## 2021-06-08 v2.2.0
* [MODPUBSUB-176](https://issues.folio.org/browse/MODPUBSUB-176) Remove everything after dash from the module ID
* [MODPUBSUB-175](https://issues.folio.org/browse/MODPUBSUB-175) Fix method that builds module ID

## 2021-06-02 v2.1.0
* [MODPUBSUB-173](https://issues.folio.org/browse/MODPUBSUB-173) Upgrade pubsub client to RMB v33.0.0
* [MODPUBSUB-171](https://issues.folio.org/browse/MODPUBSUB-171) Provide properties for Kafka security in kafka-wrapper
* [MODPUBSUB-161](https://issues.folio.org/browse/MODPUBSUB-161) Explicitly close KafkaProducer after sending message
* [MODPUBSUB-166](https://issues.folio.org/browse/MODPUBSUB-166) Fix memory leak in PubSubClientUtils
* [MODPUBSUB-78](https://issues.folio.org/browse/MODPUBSUB-78) Get system user credentials from environment variables
* [MODPUBSUB-163](https://issues.folio.org/browse/MODPUBSUB-163) Kafka Thread Blocked Timeout (KCache)
* [MODPUBSUB-155](https://issues.folio.org/browse/MODPUBSUB-155) Fix intermittent module initialization failures
* [MODPUBSUB-157](https://issues.folio.org/browse/MODPUBSUB-157) Fix query used to delete existing subscriber/publisher definitions
* [MODPUBSUB-158](https://issues.folio.org/browse/MODPUBSUB-158) Fix registration of existing subscribers upon startup
* [MODDATAIMP-372](https://issues.folio.org/browse/MODDATAIMP-372) Data Import job creates SRS records but not all expected Inventory records
* [MODINV-373](https://issues.folio.org/browse/MODINV-373) Ensure exactly once processing for interaction via Kafka
* [MODPUBSUB-152](https://issues.folio.org/browse/MODPUBSUB-152) Fix module registration failure when MessagingDescriptor contains no publications
* [MODPUBSUB-150](https://issues.folio.org/browse/MODPUBSUB-150) Use "replaces" for deprecated permissions replaced by a new permission

## 2021-06-17 v2.0.8
* [MODPUBSUB-181](https://issues.folio.org/browse/MODPUBSUB-181) Allow creating Kafka topics if tenant ID doesn't match \w{4,}

## 2021-05-17 v2.0.7
* [MODPUBSUB-166](https://issues.folio.org/browse/MODPUBSUB-166) Fix memory leak in PubSubClientUtils

## 2021-04-28 v2.0.6
* [MODPUBSUB-78](https://issues.folio.org/browse/MODPUBSUB-78) Get system user credentials from environment variables

## 2021-04-22 v2.0.5
* [MODPUBSUB-163](https://issues.folio.org/browse/MODPUBSUB-163) Kafka Thread Blocked Timeout (KCache)

## 2021-04-21 v2.0.4
* [MODPUBSUB-155](https://issues.folio.org/browse/MODPUBSUB-155) Fix intermittent module initialization failures
* [MODPUBSUB-157](https://issues.folio.org/browse/MODPUBSUB-157) Fix query used to delete existing subscriber/publisher definitions

## 2021-04-08 v2.0.3
* [MODPUBSUB-158](https://issues.folio.org/browse/MODPUBSUB-158) Fix registration of existing subscribers upon startup

## 2021-03-25 v2.0.2
* [MODINV-373](https://issues.folio.org/browse/MODINV-373) Ensure exactly once processing for interaction via Kafka.
* [MODDATAIMP-372](https://issues.folio.org/browse/MODDATAIMP-372) Data Import job creates SRS records but not all expected Inventory records

## 2021-03-06 v2.0.1
* [MODPUBSUB-152](https://issues.folio.org/browse/MODPUBSUB-152) Module registration in mod-pubsub fails when MessagingDescriptor contains no publications

## 2021-02-22 v2.0.0
* [MODPUBSUB-87](https://issues.folio.org/browse/MODPUBSUB-87) Create utility method for module unregistering
* [MODPUBSUB-118](https://issues.folio.org/browse/MODPUBSUB-118) Create sub-project in mod-pubsub for utility transport layer classes
* [MODPUBSUB-139](https://issues.folio.org/browse/MODPUBSUB-139) Fix multiple versions of mod-pubsub in the same environment consuming events for all tenants
* [MODPUBSUB-140](https://issues.folio.org/browse/MODPUBSUB-140) Upgrade to RAML Module Builder 32.x
* [MODPUBSUB-137](https://issues.folio.org/browse/MODPUBSUB-137) Add personal data disclosure form

## 2020-11-05 v1.3.3
* Fix logging after RMB upgrade

## 2020-11-03 v1.3.2
* [MODPUBSUB-129](https://issues.folio.org/browse/MODPUBSUB-129) Create script that would delete module subscriptions with "_" in their names
* [MODPUBSUB-134](https://issues.folio.org/browse/MODPUBSUB-134) Upgrade to RMB v31.1.5

## 2020-10-23 v1.3.1
* [MODPUBSUB-127](https://issues.folio.org/browse/MODPUBSUB-127) Explicitly close HttpClient created by PubsubClient

## 2020-10-06 v1.3.0
* [MODPUBSUB-102](https://issues.folio.org/browse/MODPUBSUB-102) Corrected property with logger configuration file for pub-sub client
* [MODDATAIMP-324](https://issues.folio.org/browse/MODDATAIMP-324) Update to RMB v31.0.2
* [MODPUBSUB-110](https://issues.folio.org/browse/MODPUBSUB-110) Make env variables required for module deployment.
* [MODPUBSUB-126](https://issues.folio.org/browse/MODPUBSUB-126) Make GET pubsub/history return audit messages filtered by date inclusively 

## 2020-11-03 v1.2.6
* [MODPUBSUB-133](https://issues.folio.org/browse/MODPUBSUB-133) Explicitly close HttpClient created by PubsubClient
* [MODPUBSUB-129](https://issues.folio.org/browse/MODPUBSUB-129) Create script that would delete module subscriptions with "_" in their names

## 2020-07-17 v1.2.5
* [MODDATAIMP-309](https://issues.folio.org/browse/MODDATAIMP-309) Remove unnecessary requests to login

## 2020-07-10 v1.2.4
* [MODDATAIMP-309](https://issues.folio.org/browse/MODDATAIMP-309) Deleted KafkaPublisherServiceImpl vertx proxy service
* Add dual dependency support for "login" interface 6.0 and 7.0

## 2020-06-22 v1.2.3
* [MODPUBSUB-90](https://issues.folio.org/browse/MODPUBSUB-90) Allow Subscribers to be registered before the Publishers
* [MODPUBSUB-106](https://issues.folio.org/browse/MODPUBSUB-106) Fix issue with validation of EventDescriptor - save EventDescriptor as json, not an escaped String 
* [MODPUBSUB-105](https://issues.folio.org/browse/MODPUBSUB-105) Fix failure to deliver event due to 403 error

## 2020-06-11 v1.2.2
* Update dependency on "login" interface to v7.0

## 2020-06-11 v1.2.1
* [MODPUBSUB-99](https://issues.folio.org/browse/MODPUBSUB-99) Fix reading of pubsub user credentials 
* [MODPUBSUB-105](https://issues.folio.org/browse/MODPUBSUB-105) Sometimes files don't finish processing, and it's not clear why 

## 2020-06-10 v.1.2.0
* [MODPUBSUB-82](https://issues.folio.org/browse/MODPUBSUB-82) Switch Liquibase integration to use [folio-liquibase-util](https://github.com/folio-org/folio-liquibase-util)
* [MODPUBSUB-96](https://issues.folio.org/browse/MODPUBSUB-96) Add permission to send events to mod-patron-blocks
* [MODPUBSUB-97](https://issues.folio.org/browse/MODPUBSUB-97) Allow Kafka topic name to have a customized prefix
* [MODPUBSUB-95](https://issues.folio.org/browse/MODPUBSUB-95) Allow publishing event when there are no subscribers
* [MODPUBSUB-85](https://issues.folio.org/browse/MODPUBSUB-85) Client: Remove call for registering Publisher/Subscriber if none declared in MessagingDescriptor
* [MODPUBSUB-88](https://issues.folio.org/browse/MODPUBSUB-88) Upgrade to RAML Module Builder 30.0.2

## 2020-04-27 v1.1.5
* [MODPUBSUB-83](https://issues.folio.org/browse/MODPUBSUB-83) Add env variable to set replication factor and number of partitions for topics in kafka

## 2020-04-22 v1.1.4
* Extended README documentation
* Added creating of topics on module startup
* Added saving of error messages to audit for REJECTED events
* Added utility method to construct module name on registration
* [MODPUBSUB-76](https://issues.folio.org/browse/MODPUBSUB-76) Fixed filling the mod-pubsub container filesystem
* [MODPUBSUB-80](https://issues.folio.org/browse/MODPUBSUB-80) Added override of subscriber with earlier version when new version is registered

## 2020-04-09 v1.1.3
* [MODPUBSUB-73](https://issues.folio.org/browse/MODPUBSUB-73) Fixed duplicate delivery of events

## 2020-04-03 v1.1.2
* [MODPUBSUB-71](https://issues.folio.org/browse/MODPUBSUB-71) Fixed issue with token when delivering the first event
* [MODPUBSUB-74](https://issues.folio.org/browse/MODPUBSUB-74) Switched off by default logging of event payload
* Added -XX:+HeapDumpOnOutOfMemoryError param to JAVA_OPTIONS

## 2020-03-28 v1.1.1
* Fixed permissions

## 2020-03-06 V1.1.0
* Updated RMB version to 29.1.5
* Fixed reading "MessagingDescriptor" file from JAR file
* Health check for docker-container was created
* Configured local Cache to remove redundant querying of the db for getting messaging modules 
* Replaced single shared KafkaProducer with multiple KafkaProducer instances running in WorkerVerticle
* Fixed user permissions issues

## 2019-01-21 v1.0.2
* Fixed reading "MessagingDescriptor" file from JAR file

## 2019-12-13 v1.0.1
* Removed default permissions for pub-sub user
* Updated LaunchDescriptor
* Used new base docker image
* Updated documentation

## 2019-12-04 v1.0.0
* Initial module setup
* Defined EventDescriptor and Event schemas
* Changed project structure to contain server and client parts. Client builds as a lightweight java library
* Extended Event schema
* Added samples
* Applied Liquibase scripting tool to manage database tables
* Applied Spring DI maintenance
* Added stub implementations for EventService and EventDao
* Added scripts to create module specific tables: module, event_type, messaging_module
* Defined MessagingDescriptor, PublisherDescriptor and SubscriberDescriptor schemas.
* Added PubSubClientUtil to read MessagingDescriptor file.
* Added schemas for audit trail
* Added Dao components for module schema.
* Added DAO component for tenant schema
* Added API for Event Types managing
* Added API for Publishers managing
* Added API for Subscribers managing
* Created API for retrieving audit messages
* Added preliminary cleaning of publisher/subscriber information before declaration publisher/subscriber with same module name and tenant id   
* Configured kafka client
* Removed module table, modified messagingModule table, updated schemas
* Created Publishing service
* Created Consumer service
* Created Security Manager
* Created Startup Service

 | METHOD |             URL                                                                         | DESCRIPTION                                      |
 |--------|-----------------------------------------------------------------------------------------|--------------------------------------------------|
 | GET    | /pubsub/event-types                                                                     | Get collection of Event Descriptors              |
 | POST   | /pubsub/event-types                                                                     | Create new Event Type                            |
 | GET    | /pubsub/event-types/{eventTypeName}                                                     | Get Event Descriptor of particular event type    |
 | PUT    | /pubsub/event-types/{eventTypeName}                                                     | Update Event Descriptor of particular event type |
 | DELETE | /pubsub/event-types/{eventTypeName}                                                     | Delete event type                                |
 | POST   | /pubsub/event-types/declare/publisher                                                   | Create publisher                                 |
 | DELETE | /pubsub/event-types/{eventTypeName}/publisher?moduleName={moduleName}                   | Delete publisher declaration                     |
 | GET    | /pubsub/event-types/{eventTypeName}/publishers                                          | Get collection of Publishers                     |
 | POST   | /pubsub/event-types/declare/subscriber                                                  | Create subscriber                                |
 | DELETE | /pubsub/event-types/{eventTypeName}/subscribers?moduleName={moduleName}                 | Delete subscriber declaration                    |
 | GET    | /pubsub/event-types/{eventTypeName}/subscribers                                         | Get collection of Subscribers                    |
 | GET    | /pubsub/history?startDate={startDate}&endDate={endDate}                                 | Retrieve activity history for a period of time   |
 | GET    | /pubsub/audit-messages/{eventId}/payload                                                | Get audit message payload by eventId             |
