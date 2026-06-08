# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

## [0.0.47]

### Added
- `MockTilgangskontrollRequestHandler` in test fixtures — simulates `istilgangskontroll` endpoints (`/person` and `/brukere`) based on configurable per-`NavIdent` tilgang details (`MockUserTilgangDetails`, `MockUserSyfoTilgangLevel`).
- `MockAzureAdRequestHandler` in test fixtures — simulates the Azure AD OBO token exchange endpoint by echoing the assertion token back, enabling downstream mock handlers to read the `NAVident` claim.
- `MockRequestHandlerUtils.kt` in test fixtures — generic `respond` and `receiveBody` helpers for use with the Ktor mock engine, replacing earlier `RequestUtils.kt` in test package.

### Changed
- `ApplicationCall.navIdent`, `navIdentOrThrow()`, and `navIdentOrThrow(action)` now return `NavIdent` instead of `String`.
- `MockTilgangResponse` moved to `no.nav.syfo.common.mock.tilgangskontroll` subpackage (was `no.nav.syfo.common.mock`).
- `MockAzureAdTokenResponse` moved to `no.nav.syfo.common.mock.token.azuread` subpackage (was `no.nav.syfo.common.mock`).

## [0.0.46]

### Changed
- `AuthorizedCall` renamed to `AuthorizedUser` and narrowed to carry only the authenticated user's `token` and lazily-resolved `navIdent`. The `personIdent` and `callId` are no longer part of the object.
- `checkPersonAndSyfoTilgang` block handler now receives three arguments — `AuthorizedUser`, the validated `PersonIdent`, and the `callId: String` — instead of a single `AuthorizedCall`.
- `checkPersonAndSyfoTilgang` overload that takes an explicit `personIdent` parameter now accepts `PersonIdent` instead of `String`.
- `TilgangskontrollClient.hasAccess` and `hasWriteAccess` now take `personIdent` as a `PersonIdent` instead of a `String`.
- `ApplicationCall.personIdent`, `personIdentOrThrow()` and `personIdentOrThrow(action)` now return `PersonIdent` instead of `String`. Throws `IllegalArgumentException` if the header value is present but not a valid `PersonIdent`.

## [0.0.45]

### Changed
- `TilgangskontrollClient.filterPersonsUserHasAccessTo` now accepts `List<PersonIdent>` and returns `List<PersonIdent>?` instead of `List<String>` / `List<String>?`.
- `RoutingContext.filterPersonsUserHasAccessTo` updated accordingly — `personIdenter` parameter and return type are now `List<PersonIdent>` / `List<PersonIdent>?`.

## [0.0.44]

### Added
- `PersonIdent.asProducerRecordKey()` extension function — hashes a `PersonIdent` into a deterministic UUID string (via `UUID.nameUUIDFromBytes`) for use as a Kafka record key.

## [0.0.43]

### Added
- `PersonIdent` and `NavIdent` as `@JvmInline` value classes under `no.nav.syfo.common.types.ident`.
- `AuthorizedCall` — typed context passed to the `checkPersonAndSyfoTilgang` handler block, carrying `personIdent`, `token`, `callId`, and a lazily-resolved `navIdent` (extracted from the `NAVident` token claim only on first access).
- `generateCallId()` utility function that can be used in callId plugin in apps to get same generation behavior as in library and between apps.

### Changed
- `checkPersonAndSyfoTilgang` block handler now receives `AuthorizedCall` instead of a plain `personIdent: String`.
- `callId` extension property renamed to `callIdFromHeader` to avoid confusion with similar callId extension function from plugin in apps, and changed to return a non-null `String`, generating a UUID fallback when the header is missing.

## [0.0.42]

Maintenance: Dependency cleanup — manage Jackson via `jackson-bom`, scope `micrometer-core` to `implementation`, and drop `logstash-logback-encoder` in favour of the SLF4J fluent API for structured logging.

## [0.0.41]

Maintenance: Improve workflows consistency.

## [0.0.40]

Maintenance: Replace deprecated URL() parsing constructor with URI() in ApiAuthenticationPlugin.kt.

## [0.0.39]

### Added
- `bearerTokenOrThrow()` and `bearerTokenOrThrow(action: String)` functions — throwing variants of `bearerToken`, with an optional `action` context included in the error message.
- `navIdentOrThrow()` and `navIdentOrThrow(action: String)` functions — throwing variants of `navIdent`, with an optional `action` context included in the error message.
- `personIdentOrThrow()` and `personIdentOrThrow(action: String)` functions — throwing variants of `personIdent`, with an optional `action` context included in the error message.
- `RoutingContext.filterPersonsUserHasAccessTo` extension function — convenience helper that reads the bearer token and call id from the request and delegates to `TilgangskontrollClient.filterPersonsUserHasAccessTo`.

### Changed
- `bearerTokenOrNull` renamed to `bearerToken` (nullable `String?`). The old non-nullable `bearerToken` property is replaced by `bearerTokenOrThrow()`.
- `navIdentOrNull` renamed to `navIdent` (nullable `String?`). The old non-nullable `navIdent` property is replaced by `navIdentOrThrow()`.
- `personIdentOrNull` renamed to `personIdent` (nullable `String?`). The old non-nullable `personIdent` property is replaced by `personIdentOrThrow()`.
- `callId` now returns `String?` instead of `String`. Previously returned `"unknown"` when the header was absent; now returns `null` and logs a warning.
- `consumerClientId` now returns `String?` instead of `String`. Previously returned `"unknown"` when the claim was absent; now returns `null` and logs a warning.
- `checkPersonAndSyfoTilgang` block handler now receives the validated `personIdent: String` as a parameter (was `suspend () -> Unit`, now `suspend (String) -> Unit`).
- `TilgangskontrollClient.personsUserHasAccessTo` renamed to `filterPersonsUserHasAccessTo`.
- `TilgangDeniedException` error message now reads `"Failed to $action: Access denied – User does not have required persontilgang or fagtilgang."` instead of `"Denied NAVIdent access to personident: $action"`.

## [0.0.38]

### Changed
- Removed ktor packages and move files up one level.

## [0.0.37]

### Changed
- `VeilederTilgangForbiddenException` renamed to `TilgangDeniedException`.
- `callIdOrNull` property removed. `callId` no longer throws if the `Nav-Call-Id` header is absent — returns `"unknown"` and logs a warning instead.
- `consumerClientIdOrNull()` and `consumerClientId()` functions replaced by a single `consumerClientId` property that returns `"unknown"` and logs a warning if the `azp` claim or Authorization header is absent.
- `navIdentOrNull()` and `navIdent()` functions replaced by `navIdentOrNull` and `navIdent` extension properties. `navIdent` still throws `IllegalArgumentException` if the claim is absent.

## [0.0.36]

### Changed
- Revert change made in 0.0.32 to delay moving `PersonIdent` type to library and using it with tilgangskontroll and application call extension functions.

## [0.0.35]

### Changed
- Renamed `personsVeilederHasAccessTo` to `personsUserHasAccessTo` in `no.nav.syfo.common.tilgangskontroll.client`.
- Renamed `MockTilgang` to `MockTilgangResponse` in `no.nav.syfo.common.mock` test fixtures package.

## [0.0.34]

### Added
- `OrNull` variants for all `ApplicationCall` header and token-claim readers: `bearerTokenOrNull`, `callIdOrNull`, `personIdentOrNull`, `consumerClientIdOrNull()`, `navIdentOrNull()` — return `null` instead of throwing when the header or claim is absent.

### Changed
- `getCallId()`, `getPersonIdent()`, `getBearerToken()` functions replaced by `callId`, `personIdent`, `bearerToken` extension properties.
- `getConsumerClientId()` renamed to `consumerClientId()`, `getNavIdent()` renamed to `navIdent()`.
- `bearerToken` and `personIdent` are now non-nullable and throw `IllegalArgumentException` if the header is absent. Previously `getBearerToken()` returned `String?` and `getPersonIdent()` returned `PersonIdent?`.
- `callId` now throws `IllegalArgumentException` if the header is absent. Previously `getCallId()` returned the string `"null"`.
- `consumerClientId()` now returns `String` and throws `IllegalArgumentException` if the `azp` claim is absent. Previously `getConsumerClientId()` returned `String?`.
- `navIdent()` now throws `IllegalArgumentException` instead of `Error` if the `NAVident` claim is missing.

## [0.0.33]

### Added
- Test fixtures (`testFixtures` classifier): `MockTilgang` and `MockAzureAdTokenResponse` in `no.nav.syfo.common.mock`. See [docs/testing.md](docs/testing.md).

## [0.0.32]

### Added
- `PersonIdent` value type in `no.nav.syfo.common.person`. Validates that the value is exactly 11 digits; throws `IllegalArgumentException` on invalid input.
- `ApplicationCall.getPersonIdent()` now returns `PersonIdent?` instead of `String?`. Throws `IllegalArgumentException` if the header value is present but not a valid 11-digit identity number.
- `checkPersonAndSyfoTilgang` overload that takes an explicit `personIdent` parameter now accepts `PersonIdent` instead of `String`.
- `TilgangskontrollClient.hasAccess` and `hasWriteAccess` now accept `PersonIdent` instead of `String` for the `personIdent` parameter.
- `TilgangskontrollClient.personsVeilederHasAccessTo` now accepts `List<PersonIdent>` and returns `List<PersonIdent>?`.

### Changed
- Renamed `checkVeilederTilgangToPerson` to `checkPersonAndSyfoTilgang` in the `tilgangskontroll.ktor` package.

## [0.0.31]

### Added
- Metric names are now prefixed with the consuming app's name, read from the `NAIS_APP_NAME` environment variable (injected by NAIS from `metadata.name` in `app.yaml`). Dashes in the app name are replaced with underscores to comply with Prometheus naming rules. Falls back to `unknown` if the variable is not set.

## [0.0.30]

### Changed
- Moved `VeilederTilgangForbiddenException` from `tilgangskontroll.ktor` to `tilgangskontroll`.

## [0.0.29]

### Changed
- Renamed `checkVeilederTilgang` to `checkVeilederTilgangTilPerson` in `tilgangskontroll.ktor` package.

## [0.0.28]

### Changed
- Jackson dependencies (`jackson-module-kotlin`, `jackson-datatype-jsr310`) promoted from `implementation` to `api` — consumers no longer need to declare Jackson themselves to use `configuredJacksonMapper()` or `applyCommonJacksonConfig()`.

## [0.0.27]

### Changed
- `NAV_CALL_ID_HEADER` and `NAV_PERSONIDENT_HEADER` and `bearerHeader()` in `no.nav.syfo.common.util` are now public.

## [0.0.26]

### Added
- `ClientConfig` data class in `no.nav.syfo.common.util` — holds `baseUrl` and `clientId` for configuring downstream service clients.

## [0.0.25]

### Changed
- `AzureAdClient`: no longer requires `AzureEnvironment` to be passed in — reads NAIS environment variables automatically via `AzureAdClientConfig.fromEnv()`. Constructor argument is optional and can still be overridden (e.g. in tests).
- `AzureEnvironment` renamed to `AzureAdClientConfig`.

## [0.0.24]

### Changed
- `TilgangskontrollClient`: `TILGANGSKONTROLL_PERSON_PATH` and `TILGANGSKONTROLL_BRUKERE_PATH` are now public constants.

## [0.0.23]

### Changed
- `JWT_CLAIM_NAVIDENT` is now public — consumer apps can reference the claim name constant directly.

## [0.0.22]

### Changed
- Publish workflow: triggers automatically on push to main, skips if version already tagged, creates git tag after successful publish. Manual dispatch restricted to main branch.
- `build.gradle.kts`: publish task blocked when not running in GitHub Actions CI.

## [0.0.21]

### Added
- `SystemTokenProvider` interface (`no.nav.syfo.common.token`) for typed system/M2M token dependencies.
- `no.nav.syfo.common.token.texas` package with `EntraIdClient` — replaces the previous `entraid.EntraIdClient`. Uses the same Texas sidecar, but endpoint URLs are now always read from env vars (`NAIS_TOKEN_EXCHANGE_ENDPOINT`, `NAIS_TOKEN_ENDPOINT`) and are not overridable by consumers.
- `TexasIdentityProvider` enum with a note on how to extend with TokenX and Maskinporten in the future.

### Changed
- `AzureAdClient` now implements both `OboTokenProvider` and `SystemTokenProvider`. `getSystemToken` return type changed from `AzureAdToken?` to `String?` (the access token string directly).
- `EntraIdClient` moved from `no.nav.syfo.common.token.entraid` to `no.nav.syfo.common.token.texas`.

### Removed
- `no.nav.syfo.common.token.entraid` package (`EntraIdClient` and `EntraIdTokenModels`).

## [0.0.20]

### Changed
- `OboTokenProvider`: renamed parameter `scopeClientId` → `targetClientId` for consistency.
- `EntraIdClient.getOnBehalfOfToken`: now accepts `targetClientId` in `<cluster>.<namespace>.<app>` format (same as `AzureAdClient`) and constructs the full `api://$targetClientId/.default` scope internally.
- `AzureAdClient.getOnBehalfOfToken`: aligned parameter name to `targetClientId`.

## [0.0.19]

### Added
- `no.nav.syfo.common.auth` package with shared JWT authentication utilities:
  - `WellKnown` — OpenID Connect discovery document data class
  - `getWellKnown()` — fetches and parses the discovery document at startup
  - `JwtIssuer` and `JwtIssuerType` — issuer configuration for Ktor JWT authentication
  - `installJwtAuthentication()` — Ktor plugin that configures JWT validation per issuer

## [0.0.18]

### Changed
- `TilgangskontrollClient`: removed `meterRegistry` constructor parameter — counters are registered on `Metrics.globalRegistry` at startup.

## [0.0.17]

### Added
- `EntraIdClient`: new token client using the Nais token exchange sidecar (Texas) (`NAIS_TOKEN_EXCHANGE_ENDPOINT` for OBO, `NAIS_TOKEN_ENDPOINT` for M2M). No client credentials or caching needed — Texas handles it.
- `OboTokenProvider` interface moved to `no.nav.syfo.common.token` (shared by both `AzureAdClient` and `EntraIdClient`).

### Changed
- Moved `AzureAdClient` and related classes from `no.nav.syfo.common.azure` to `no.nav.syfo.common.token.azuread`.
- Updated README with token provider guidance, `EntraIdClient` usage, and corrected class names.

## [0.0.16]

### Changed
- `AzureAdClient`: added cache hit/miss metrics for system token requests (`call_azuread_system_token_cache_hit_count`, `call_azuread_system_token_cache_miss_count`), registered on `Metrics.globalRegistry`.
- `AzureAdClient`: split error handling into separate `ClientRequestException` (4xx) and `ServerResponseException` (5xx) catch blocks for more informative error logging.

## [0.0.15]

### Changed
- Added KDoc to all public API members.
- Renamed `veilederPersonerAccess()` to `personsVeilederHasAccessTo()`.
- Renamed parameter `personident` to `personIdent` in `hasAccess()` and `hasWriteAccess()`.
- Renamed parameter `personidenter` to `personIdenter` in `personsVeilederHasAccessTo()`.

## [0.0.14]

### Changed
- Renamed `HttpClientCommon.kt` to `CommonHttpClient.kt`.
- Renamed `httpClientDefault()` to `defaultHttpClient()` and `httpClientProxy()` to `proxyHttpClient()` for more idiomatic Kotlin naming (qualifier first, type second).
- Made `defaultHttpClient()`, `proxyHttpClient()`, and `commonConfig` public so consumers can use them directly.

## [0.0.13]

### Changed
- Renamed `ApplicationCall.getNAVIdent()` to `getNavIdent()` for consistent camelCase naming.
- Renamed `ApplicationCall.getBearerHeader()` to `getBearerToken()` to better reflect what it returns (the token string, not a header).
- Renamed `ApplicationCall.getPersonident()` to `getPersonIdent()` for consistent camelCase naming.

## [0.0.12]

### Added
- `OboTokenProvider` functional interface (`fun interface`) in `no.nav.syfo.common.azure`. Represents any supplier of OBO (on-behalf-of) access tokens. Accepts `scopeClientId` and `token`, returns the access token as `String?`.
- `AzureAdClient` now implements `OboTokenProvider`, so it can be passed directly to `TilgangskontrollClient` without a lambda wrapper.

### Changed
- `TilgangskontrollClient` now depends on `OboTokenProvider` instead of `AzureAdClient` directly. This decouples the client from the Azure AD implementation and makes it easier to test or substitute with a custom token provider.
- `AzureAdClient.getOnBehalfOfToken()` now returns `String?` (the access token string) instead of `AzureAdToken?`. Callers that previously accessed `.accessToken` on the result must remove that property access.

## [0.0.11]

### Changed
- Renamed `ObjectMapper.configure()` extension function to `applyCommonJacksonConfig()` to avoid conflict with Jackson's built-in `configure(Feature, Boolean)` method.

## [0.0.10]

### Changed
- Renamed `ForbiddenAccessVeilederException` to `VeilederTilgangBlokkertException` for consistency with Norwegian naming conventions used elsewhere in the codebase.

## [0.0.9]

### Changed
- Renamed `ForbiddenAccessVeilederException` to `VeilederTilgangBlokkertException` (initial rename, superseded by 0.0.10).

## [0.0.8]

### Added
- `TilgangskontrollClient` with `hasAccess()`, `hasWriteAccess()`, and `veilederPersonerAccess()` methods.
- `PipelineUtil` with `checkVeilederTilgang()` and `checkVeilederTilgangWithAction()` pipeline helpers.
- `ObjectMapperConfig` (`configuredJacksonMapper()`) and `JacksonMapperConfig` (`applyCommonJacksonConfig()`).
- `AzureAdClient`, `AzureAdToken`, `AzureAdTokenResponse`, `AzureEnvironment` for Azure AD OBO token exchange.
- `RequestUtil` with common NAV header constants and helpers.
- `HttpClientCommon` with `httpClientDefault()` and `httpClientProxy()` preconfigurations.

