# Changelog

Release history for the Azure IoT Hub Service SDK for Java,
`com.microsoft.azure.sdk.iot:iot-service-client`.

This history was reconstructed on 2026-10-30. It covers all 81 versions in
[Maven Central's version inventory](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/maven-metadata.xml),
including the two 2.0.0 previews, plus the four numbered releases of the
[legacy preview package](#legacy-preview-package).
The pending 2.1.11 release is also documented below.
Device SDK, Device Provisioning Service SDK, and parent-POM versions are not
service SDK releases and are not listed separately.

Published-release dates below are the UTC dates in Maven Central's
[artifact-directory listing](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/).
They can differ from GitHub announcement dates, release-tag names, and
version-bump commit dates. Entries are ordered by version, not strictly by date;
preview releases and LTS backports therefore overlap chronologically.

Versions through 2.1.6 were released from
[Azure/azure-iot-sdk-java](https://github.com/Azure/azure-iot-sdk-java/releases).
This standalone repository has no GitHub releases or tags as of the
reconstruction date. Historical release notes, published POMs/source JARs, and
the linked repository changes are used as evidence; imported or catch-up
commits are not counted as new releases.

## 2.1.11 - Unreleased

Release preparation is tracked in PR #53. No Maven Central publication date
is available as of 2026-10-30.

Includes changes on `main` through
[`c7c2f5d` (2026-10-28)](https://github.com/Azure/azure-iot-service-sdk-java/commit/c7c2f5d5bc74a5b23b125edb42bcde1f827f495a),
after the published 2.1.10 package:

- Fix service clients failing to recover when IoT Hub detaches an AMQP link
  without closing the connection, causing `MessagingClient.open()` to time
  out. Handle unexpected link detaches and closures, including events without
  an error condition, and close the affected connection so messaging,
  feedback, and file-upload notification clients can be reopened.
- Release the previous reactor before reopening a client, clear stale
  connection errors, and avoid waiting for a reactor that has already stopped.
- Reject negative HTTP read/connect timeout values when building configuration,
  digital-twin, scheduled-job, direct-method, query, registry, and twin client
  options. Preserve the existing defaults and acceptance of zero.
- Update Netty from 4.1.100.Final to 4.1.130.Final for samples/integration tests,
  and remove UDT references from the test proxy implementation.

Sources: [2.1.11 release preparation, PR #53](https://github.com/Azure/azure-iot-service-sdk-java/pull/53),
[AMQP recovery, PR #52](https://github.com/Azure/azure-iot-service-sdk-java/pull/52),
[reopen timeout, issue #51](https://github.com/Azure/azure-iot-service-sdk-java/issues/51),
[timeout validation, PR #49](https://github.com/Azure/azure-iot-service-sdk-java/pull/49),
[test dependencies, PR #48](https://github.com/Azure/azure-iot-service-sdk-java/pull/48).

## 2.1.10 - 2025-12-03

- Fix direct-method invocation timeouts: HTTP read and connect timeouts are at
  least the per-request method response and connection timeouts, respectively,
  rather than always using the client-level values.
- Prevent a null dereference when a scheduled direct-method job contains an
  outcome but omits `cloudToDeviceMethod`.

Sources: [published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/2.1.10/),
[PR #43](https://github.com/Azure/azure-iot-service-sdk-java/pull/43),
[PR #44](https://github.com/Azure/azure-iot-service-sdk-java/pull/44).

## 2.1.9 - 2024-10-03

- Serialize `Message.expiryTimeUtc` into the AMQP message's absolute-expiry-time
  property, allowing IoT Hub to apply the requested cloud-to-device expiration.

Sources: [published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/2.1.9/),
[PR #35](https://github.com/Azure/azure-iot-service-sdk-java/pull/35).

## 2.1.8 - 2024-08-09

- Restore `Message.expiryTimeUtc` and its getter/setter. Forwarding this value
  onto the AMQP wire is fixed separately in 2.1.9.
- Expose `Message.DEFAULT_IOTHUB_MESSAGE_CHARSET` as UTF-8.
- Reject a null byte-array message body with `IllegalArgumentException`.

Sources: [published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/2.1.8/),
[PR #32](https://github.com/Azure/azure-iot-service-sdk-java/pull/32).

## 2.1.7 - 2024-01-11

- Update the published Proton-J dependency from 0.34.0 to 0.34.1.
- Update the published Guava dependency from 31.1-jre to 32.0.0-jre.
- Add a repository sample for deleting devices from an IoT hub.
- Publish from the standalone service SDK repository. Comparison with the
  2.1.6 source JAR shows no substantive SDK Java implementation changes; fixes
  imported from the predecessor repository belong to their original releases
  below, not to this version.

Sources: [2.1.6 published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/2.1.6/),
[2.1.7 published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/2.1.7/),
[device-deletion sample, PR #14](https://github.com/Azure/azure-iot-service-sdk-java/pull/14).

## 2.1.6 - 2023-01-25

- Fix `TwinClient.replace` when the client authenticates with
  `AzureSasCredential`.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2023-01-23).

## 2.1.5 - 2022-12-12

- Update transitive dependencies.
- Add `Automatic-Module-Name` to the JAR manifest for Java module-system
  compatibility.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-12-12).

## 2.1.4 - 2022-10-14

- Update the Jackson dependency.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-10-13).

## 2.1.3 - 2022-09-29

- Correct the serialized values of the authentication-type enum.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-09-28).

## 2.1.2 - 2022-09-14

- Update the Proton-J and Proton-J extensions dependencies.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-09-13).

## 2.1.1 - 2022-08-25

- Correct serialization of `maxExecutionTimeInSeconds` so the service receives
  and honors the scheduled job's execution-time limit.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-08-25).

## 2.1.0 - 2022-07-08

- Add missing fields and corresponding accessors to `Twin` and `TwinState`.
- Update Gson to 2.9.0.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-07-07).

## 2.0.2 - 2022-04-19

- Update the Guava and Jackson dependencies.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-04-18).

## 2.0.1 - 2022-04-06

- Preserve integer configuration values instead of converting them to
  floating-point values during serialization/deserialization.
- Correct configuration-content serialization.
- Allow registry device responses that omit authentication details.
- Expose twin metadata that was received from the service but not previously
  returned to callers.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-04-06).

## 2.0.0 - 2022-03-30

**Breaking changes relative to 1.x:**

- Reorganize service APIs into focused clients, including `RegistryClient`,
  `ConfigurationsClient`, `TwinClient`, `DirectMethodsClient`,
  `MessagingClient`, `ScheduledJobsClient`, and `QueryClient`.
- Replace feedback/file-upload notification receiver polling with
  `MessageFeedbackProcessorClient` and
  `FileUploadNotificationProcessorClient` callbacks.
- Remove deprecated APIs and asynchronous wrappers around synchronous service
  calls; applications supply their own asynchronous integration.
- Use the platform trust store instead of bundled root certificates. Remove
  Bouncy Castle certificate-parsing dependencies and fold the former
  `iot-deps` classes into the client libraries.
- Reduce the default SAS token lifetime from one year to one hour.
- Remove unnecessary synchronization so one service client can issue
  concurrent service requests.
- Keep dates returned by the service in UTC instead of converting them to
  the local time zone.

Sources: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-3-30),
[service-client migration guide](https://github.com/Azure/azure-iot-sdk-java/blob/main/SDK%20v2%20migration%20guide.md#iot-hub-service-client).

## 2.0.0-preview-002 - 2021-09-16

- Remove asynchronous service-client APIs.
- Stop validating symmetric-key format in the SDK.

Source: [upstream preview release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-09-16).

## 2.0.0-preview-001 - 2021-08-31

- Introduce the first 2.0.0 preview, with breaking API changes.
- Read trusted roots from the platform certificate store rather than SDK
  source.
- Remove deprecated APIs and checked exceptions that the APIs do not throw.

Source: [upstream preview release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-08-30).

## 1.34.2 - 2022-03-04

- Final 1.x LTS release.
- Update `iot-deps` to 0.15.3.

Sources: [upstream LTS release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-03-04),
[v2 migration guidance](https://github.com/Azure/azure-iot-sdk-java/blob/main/SDK%20v2%20migration%20guide.md#what-will-happen-to-the-v1-sdk).

## 1.34.1 - 2022-01-12

- Remove the unintended 16 KB message-size restriction when using AMQP over
  WebSockets.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2022-01-12).

## 1.34.0 - 2021-12-18

- Add the missing `moduleContent` field to `ConfigurationContent`.
- Make SDK thread names easier to identify.
- Log the SDK version when running a client.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-12-15).

## 1.33.2 - 2021-10-13

- Fix file-upload notification receives returning a previous notification
  when no new notification is available.
- Avoid acknowledging multiple notifications while returning only one to
  the caller.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-10-13).

## 1.33.1 - 2021-09-15

- Remove SDK-side symmetric-key format validation.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-09-15_0).

## 1.33.0 - 2021-08-03

- Update the shared `iot-deps` dependency.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-08-03).

## 1.32.0 - 2021-06-17

- LTS release.
- Trust the DigiCert Global Root G2 certificate.
- Update Bouncy Castle dependencies to 1.69.
- Fix failures on subsequent receives of file-upload notifications and
  cloud-to-device feedback messages.

Source: [upstream LTS release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/lts_7_2021).

## 1.31.1 - 2021-06-07

- Update Bouncy Castle dependencies to 1.65.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-06-07).

## 1.31.0 - 2021-05-24

- Expose twin metadata properties used with configurations.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-05-24).

## 1.30.0 - 2021-05-11

- Support user-assigned managed identities for IoT Hub import/export jobs.
- Update `iot-deps` to 0.12.0.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-05-10).

## 1.29.0 - 2021-05-06

- Add parent-scope support.
- Support role-based authentication credentials across service clients.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-5-5).

## 1.28.0 - 2021-03-04

- Add an API for replacing a twin.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-03-04).

## 1.27.2 - 2021-02-24

- Update the shared `iot-deps` dependency.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-02-24).

## 1.27.1 - 2021-02-05

- Honor a custom `SSLContext` when sending cloud-to-device messages.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-02-04).

## 1.27.0 - 2021-01-21

- Allow service clients to use a caller-provided `SSLContext`.
- Update Commons Codec and Jackson dependencies.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2021-01-20).

## 1.26.0 - 2020-10-01

- Add `DigitalTwinClient` for IoT Plug and Play operations on DTDL components,
  properties, and commands.
- Add twin model-ID support.
- Make HTTP connection/read timeouts configurable for twin and direct-method
  clients.
- Prevent a null-pointer failure when sending a cloud-to-device message
  without a payload.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2020-10-01).

## 1.25.0 - 2020-08-13

- Correct character-encoding handling in
  `IotHubServiceSasToken.buildToken`.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2020-08-13).

## 1.24.0 - 2020-07-07

- LTS release.
- Allow HTTP read/connect timeouts to be configured for registry operations.

Source: [upstream LTS release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/lts_7_2020).

## 1.23.0 - 2020-06-16

- Move service-client operations to the updated service API version.
- Expose fully qualified service error codes in HTTP exceptions.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2020-06-12).

## 1.22.0 - 2020-05-18

- Add proxy support for registry and service-client operations.
- Support arrays in twins.
- Recognize AMQP acknowledgements of subscription messages correctly.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2020-05-15).

## 1.21.1 - 2020-03-31

- Clarify jobs API documentation for key-based storage authentication.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2020-03-30).

## 1.21.0 - 2020-03-14

- Add `JobProperties.StorageAuthenticationType` for selecting how import/export
  jobs authenticate to storage.
- Add `JobProperties` overloads for registry import/export operations and
  `createForImportJob` / `createForExportJob` factory methods.
- Initially gate identity-based storage authentication behind
  `EnableStorageIdentity=1`.
- Update the shared `iot-deps` dependency.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2020-03-13).

## 1.20.1 - 2020-02-29

- Improve logging for cloud-to-device sending, feedback reception, and
  file-upload notification reception.
- Clean up file-upload notification sockets when connection establishment
  fails.
- Fix feedback and file-upload notification receivers occasionally dropping
  messages silently.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2020-02-28).

## 1.20.0 - 2020-02-12

- Remove SDK-side validation of maximum twin depth.

The upstream announcement mistakenly labels its service-client heading
`1.12.0`; its artifact link and the tagged POM identify this release as
`1.20.0`.

Sources: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2020-02-12),
[tagged version properties](https://github.com/Azure/azure-iot-sdk-java/blob/2020-02-12/pom.xml).

## 1.19.0 - 2019-12-04

- Add an API to set configuration metrics on `Configuration`.
- Remove SDK-side twin key/value restrictions while leaving service-side
  validation in place.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2019-12-4).

## 1.18.0 - 2019-09-30

- Map HTTP 409 responses to `IotHubConflictException`.
- Populate connection state and configurations on queried twin objects.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2019-09-30_2).

## 1.17.1 - 2019-06-03

- Correct parsing and propagation of connection state into
  `DeviceTwinDevice`.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2019-06-04).

## 1.17.0 - 2019-05-15

- Add device security-scope support and update the service API version.
- Update Proton-J to 0.30.0.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2019-5-14).

## 1.16.3 - 2019-04-20

- Prevent AMQP delivery tags from wrapping into negative integers.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2019-4-19).

## 1.16.2 - 2019-04-05

- Make `IotHubConnectionStringBuilder` thread-safe.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2019-4-5).

## 1.16.1 - 2019-02-28

- Avoid double-escaping JSON payloads in `MethodParser`.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2019-2-28).

## 1.16.0 - 2019-01-24

- LTS release.
- Report device capabilities in twins.
- Remove inferred exception details from HTTP exceptions.
- Improve stream cleanup, use cryptographically secure key generation, and
  avoid constructor calls to overridable methods with sensitive data.

Source: [upstream LTS release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/LTS_01_2019_Rev01).

## 1.15.2 - 2018-12-04

- Add Android API 28 support.
- Correct the Android user-agent string.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2018-12-3).

## 1.15.1 - 2018-10-11

- Avoid deserializing unnecessary fields.
- Report failures to establish AMQP connections instead of silently
  continuing.
- Preserve module IDs in module-twin query results.
- Deprecate `updateDesiredProperties`; use `updateTwin` instead.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2018-10-11).

## 1.15.0 - 2018-08-30

- Add `ExportImportDevice` accessors for twin tags and desired/reported
  properties, and include tags in its serialized representation.
- Add `SqlQuery.FromType.MODULES` for queries over `devices.modules`.
- Close the AMQP transport tail when the remote peer closes a send connection.
- Update `iot-deps` from 0.6.2 to 0.7.0.

No separate upstream announcement for 1.15.0 was found. These changes are
reconstructed from the published source JARs and POM, rather than borrowing
the distinct 1.15.1 release notes.

Sources: [1.14.1 source JAR](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/1.14.1/iot-service-client-1.14.1-sources.jar),
[1.15.0 published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/1.15.0/).

## 1.14.1 - 2018-08-10

- Fix `RegistryManager.removeDevice` incorrectly requiring a double-quoted
  ETag.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2018-8-10).

## 1.14.0 - 2018-06-22

- Add create/read/update/delete operations for IoT Edge devices and modules.
- Add configuration CRUD operations.
- Update service endpoints to the API version supporting IoT Edge.

Source: [upstream release notes, tag `on`](https://github.com/Azure/azure-iot-sdk-java/releases/tag/on).

## 1.13.4 - 2018-06-15

- Relax overly strict X.509 thumbprint-format validation.
- Correct job-type capitalization that prevented import/export jobs from
  working.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2018-6-15).

## 1.13.3 - 2018-05-25

- Fix desired-property updates that could leave some properties unchanged.
- Allow jobs to delete twin properties.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2018-5-25).

## 1.13.2 - 2018-05-04

- Make setting a desired property to null delete it from the device twin.
- Fix server authentication.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2018-5-4).

## 1.13.1 - 2018-04-14

- Update the AMQP user-agent string.
- Shut down the executor service when `RegistryManager.close` is called.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2018-4-13).

## 1.13.0 - 2018-02-23

- Update `iot-deps` from 0.4.0 to 0.5.0.
- Refresh the service version reported by the SDK. The published Java source
  differs from 1.12.0 only in this version value; the upstream service section
  does not list additional feature or bug changes.

Sources: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2018-2-23),
[1.12.0 published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/1.12.0/),
[1.13.0 published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/1.13.0/).

## 1.12.1 - 2018-05-04

- LTS maintenance release containing the server-authentication fix.

Source: [upstream LTS release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/LTS_01_2018_Rev01).

## 1.12.0 - 2018-02-10

- Replace `TwinParser` with `TwinState` and expose twin-collection versions in
  the public service API.
- Deprecate `getDevices` and `getDevicesAsync`.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2018-2-9).

## 1.11.0 - 2017-12-04

- Allow `QueryCollection` queries to return empty results without throwing.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-12-2).

## 1.10.28 - 2017-10-24

- Update the service API version to `2017-07-11`.
- Expose and accept continuation tokens for device-twin queries.
- Handle job results whose device ID is null.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-10-23).

## 1.9.27 - 2017-10-06

- Populate the missing ETag when retrieving a twin.
- Allow job queries without job-type or job-status filters.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-10-6).

## 1.9.26 - 2017-09-26

- Add a `DeviceTwinDevice` constructor that does not require a device ID.
- Handle unknown job types.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-9-25).

## 1.9.25 - 2017-09-08

- Rename the SDK's Base64 API to avoid Android compatibility conflicts.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-9-8).

## 1.9.24 - 2017-08-25

- Add X.509 authentication support to service-client CRUD operations.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-8-25).

## 1.8.24 - 2017-08-11

- Replace external Base64 libraries with an SDK implementation.
- Parse and expose the device-twin version as read-only data.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-8-11).

## 1.7.23 - 2017-07-28

- Add scheduled-job operations to direct-method and device-twin clients.
- Add job samples and end-to-end coverage for jobs and twin queries.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-7-28).

## 1.6.23 - 2017-07-14

- Support querying device jobs and job responses.
- Add jobs support for cloud-to-device methods.
- Clean up imports and service-client Javadoc issues.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-7-14).

## 1.5.22 - 2017-06-30

- Add device-twin queries.
- Add jobs support for twin operations.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-6-30).

## 1.4.22 - 2017-06-16

- Add a Jobs/V2 URL getter in preparation for jobs APIs.
- Add file-upload end-to-end tests.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-6-16).

## 1.4.21 - 2017-06-03

- Fix direct-method invocation with response timeouts longer than 24 seconds.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-6-2).

## 1.4.20 - 2017-05-19

- Add file-upload notification support.
- Add twin end-to-end tests and reorganize unit-test packages.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-5-19).

## 1.3.19 - 2017-04-24

- Add twin update support.
- Add direct-method end-to-end tests and reorganize test directories.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-4-21).

## 1.2.18 - 2017-04-07

- Expose registry statistics through getters.
- Narrow applicable exception declarations to `IOException`.
- Update Proton-J to 0.18.0.
- Throw an exception when service-client AMQP authentication fails.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-4-6).

## 1.2.17 - 2017-03-29

- Move integration tests into a separate module so test code and its
  dependencies are not shipped in the client package.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-3-28).

## 1.2.16 - 2017-03-25

- Add direct-method APIs and a direct-method sample.
- Fix message-feedback receiver timeouts.
- Align package names with the directory layout.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-3-24).

## 1.1.15 - 2017-03-10

- Add device-twin support and a service-side twin sample.
- Add twin serialization, URL building, and HTTP PATCH through
  `X-HTTP-Method-Override`.
- **Breaking:** rename the package from
  `com.microsoft.azure.sdk.iot.service.sdk` to
  `com.microsoft.azure.sdk.iot.service`.
- Settle feedback messages and throw `IotHubException` when an AMQP send
  fails.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-3-10).

## 1.0.14 - 2017-02-25

- Fix exception handling in the Proton AMQP transport.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-2-24).

## 1.0.13 - 2017-01-28

- Correct parsing of the server's `statusCode` JSON value after sending.
- Correct dependency versions in sample POMs and update links for the
  reorganized source layout.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-1-27).

## 1.0.12 - 2017-01-11

- Reorganize service-client directories to follow Maven conventions.
- This is the earliest version listed under the current artifact coordinates
  in Maven Central.

Source: [upstream release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2017-01-11).

## Legacy preview package

These releases used the separate Maven artifact
`com.microsoft.azure.sdk.iot:iot-service-client-preview`; their version numbers
must not be confused with those of `iot-service-client`.
Dates come from the
[legacy artifact-directory listing](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client-preview/).
The literal `${iot-service-client-version}` entry in its metadata is not
included because it is not a numbered release.

### 1.2.0 - 2020-10-15

- Update the IoT Hub API version for IoT Plug and Play.
- Add `DigitalTwinClient` operations for DTDL components, properties, and
  commands.
- Make twin and direct-method HTTP read/connect timeouts configurable.
- Incorporate fixes for empty cloud-to-device payloads and SAS-token
  character encoding.

Source: [upstream preview release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2020-10-15).

### 1.1.0 - 2020-07-08

- Add the IoT Plug and Play summer-refresh model-ID support.
- Add configurable registry HTTP timeouts, registry/service proxy support,
  fully qualified error codes, twin arrays, and identity-based storage
  authentication for jobs.
- Bring in the bug fixes from the stable 1.24.0 service client.

Source: [upstream preview release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2020-07-08-preview).

### 1.0.1 - 2019-12-03

- Correct the reported client identifier and version to
  `iot-service-client-preview/1.0.1`.
- The published Java source otherwise matches preview 1.0.0; the shared
  announcement does not identify additional service-client functionality.

Sources: [upstream preview release notes](https://github.com/Azure/azure-iot-sdk-java/releases/tag/2019-11-21-preview),
[1.0.0 published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client-preview/1.0.0/),
[1.0.1 published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client-preview/1.0.1/).

### 1.0.0 - 2019-11-21

- First numbered publication of the separate preview artifact.
- Compared with stable 1.18.0, add `Configuration.setMetrics` and remove
  SDK-side twin-key character/length restrictions.
- Document at-least-once delivery for file-upload notifications.

This entry is reconstructed from published source JARs; no standalone
service-client announcement for preview 1.0.0 was found.

Sources: [stable 1.18.0 source JAR](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client/1.18.0/iot-service-client-1.18.0-sources.jar),
[preview 1.0.0 published artifacts](https://repo.maven.apache.org/maven2/com/microsoft/azure/sdk/iot/iot-service-client-preview/1.0.0/).
