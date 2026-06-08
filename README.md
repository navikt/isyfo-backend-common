# isyfo-backend-common

Shared Kotlin utility library for iSyfo backend Ktor services. Intended to grow with shared backend utilities over time.

## What it provides

### Token providers
- `AzureAdClient` — acquires OBO and system tokens with direct calls to Entra Id (formerly Azure AD)
- `EntraIdClient` — acquires OBO and system tokens from Entra ID via the Nais token exchange sidecar (Texas)
- Both implement `OboTokenProvider` and `SystemTokenProvider`

### Tilgangskontroll
- `TilgangskontrollClient` — read/write access checks against `istilgangskontroll`
- Ktor `RoutingContext` extension helpers such as `checkPersonAndSyfoTilgang(...)`

### Ktor helpers for accessing request data
`ApplicationCall` extension properties and functions for extracting common request data: Bearer token, `NAVident` and `azp` JWT claims, the `nav-personident` header, and the `Nav-Call-Id` tracing header.

### Common http client configuration
`defaultHttpClient()` and `proxyHttpClient()` provide pre-configured Ktor `HttpClient` instances with Jackson content negotiation, automatic retry on non-4xx errors, and proxy support for internet-bound calls.

### Common Jackson object mapper
`configuredJacksonMapper()` and `applyCommonJacksonConfig()` provide a shared Jackson `ObjectMapper` configured with Java 8 time support, ISO-8601 date serialization, and unknown-property tolerance.

### JWT authentication
`installJwtAuthentication()` is a Ktor plugin that validates incoming JWTs, configured via `JwtIssuer` / `JwtIssuerType` and the OpenID Connect discovery document fetched by `getWellKnown()` at startup.

## Adding the dependency in consumer apps

In the consumer app, add the following dependency coordinates to `build.gradle.kts`:

```kotlin
implementation("no.nav.syfo:isyfo-backend-common:<version>")
```

Also add the GitHub Packages repository so Gradle knows where to fetch it from, and provide credentials for reading packages from GitHub, for example:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/navikt/isyfo-backend-common")
        credentials {
            username = providers.gradleProperty("githubUser").orNull
            password = providers.gradleProperty("githubPassword").orNull
        }
    }
}
```

In CI, the `kotlin-build-deploy` workflow from `isworkflows` sets the environment variables `ORG_GRADLE_PROJECT_githubUser` and `ORG_GRADLE_PROJECT_githubPassword`, which makes Gradle set the corresponding Gradle project properties referenced above.

Locally, you can set the env var `ORG_GRADLE_PROJECT_githubPassord` or the Gradle property `githubPassword` to a GitHub personal access token (PAT) with the `read:packages` scope. You can use the same PAT that you use with `NPM_AUTH_TOKEN` in frontend projects. This will allow you to run Gradle tasks that need to fetch the library in consuming apps.

---

## Instrumentation

### Logging

The library uses SLF4J for logging and does not depend on any specific logging backend. Consuming apps own the binding (e.g. Logback).

### Metrics

The library registers Micrometer counters on `Metrics.globalRegistry`. For these counters to appear in Prometheus scraping, the consuming app's `PrometheusMeterRegistry` must be wired into the global registry early in startup:

```kotlin
Metrics.addRegistry(METRICS_REGISTRY)
```

Without this, library counters will not be visible at the `/metrics` endpoint and will not be scraped.

---

## Development

### Releasing a new version

1. Bump `version` in `build.gradle.kts` and update the changelog.
2. Open a PR and merge to `main`.
3. The [Publish workflow](.github/workflows/publish.yml) runs automatically on merge — it detects the new version, runs lint and tests, publishes to GitHub Packages, and creates a `v<version>` tag.

Merges to `main` without a version bump (e.g. docs changes) are skipped silently — the workflow checks whether the version tag already exists before doing anything.

### Useful Gradle tasks

```bash
./gradlew check          # Runs all verification: tests + ktlintCheck
./gradlew ktlintFormat   # Auto-formats code with ktlint
./gradlew build          # Full build: compile + check + jar
```

In IntelliJ IDEA, you can run Gradle tasks from Gradle tool window.

### Testing library changes in a consumer app locally

You can test changes you have made in the library in an app that consumes the library without first publishing a new version to GitHub Packages.

First set version in `build.gradle.kts` in library (this repo) to a new version that does not exist yet on GitHub Packages. For example:

```kotlin
group = "no.nav.syfo"
version = "0.0.50" // A version number not found on GitHub packages
description = "Shared Kotlin utility library for iSyfo backend Ktor services."
```

Then use the Gradle task `publishToMavenLocal` to build and install the library with current code into your local Maven cache (`~/.m2`).

```bash
# In this repo — publish current state to local cache
./gradlew publishToMavenLocal
```

Then reference this local library version from the consumer app.  In the consumer's `build.gradle.kts`, update the version of the library to the version you published locally, and add `mavenLocal()` to the bottom of the repositories block.

**build.gradle.kts file in consumer app**
```kotlin
val isyfoBackendCommon = "0.0.50" // same version you published locally

// [...]

repositories {
    // other maven repositories
    mavenLocal()  // picks up locally published version not found in the repositories above
}
```

In IntelliJ, you might need to *Sync Gradle Changes* after editing `build.gradle.tks` in the consuming app. Click the sync button that appears in top right corner, or find the **Sync All Gradle Projects** action.

Remove `mavenLocal()` when done testing, or leave it for convenience later.

---

## Package documentation

- [Token providers](docs/token.md) — `EntraIdClient` (Texas) and `AzureAdClient`
- [Veileder tilgangskontroll](docs/tilgangskontroll.md) — `TilgangskontrollClient` and Ktor helpers
- [JWT authentication](docs/auth.md) — `installJwtAuthentication`, `getWellKnown`, `JwtIssuer`
- [Testing utilities](docs/testing.md) — `MockTilgang`, `MockAzureAdTokenResponse` (test fixtures)
