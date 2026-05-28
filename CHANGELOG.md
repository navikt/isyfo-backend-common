# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

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
- **Breaking**: `AzureAdClient.getOnBehalfOfToken()` now returns `String?` (the access token string) instead of `AzureAdToken?`. Callers that previously accessed `.accessToken` on the result must remove that property access.

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

