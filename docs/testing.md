# Testing utilities

The library ships a `test-fixtures` artifact containing mock helpers for use in integration tests of consuming apps.

## Setup

Add the dependency in `build.gradle.kts`:

```kotlin
testImplementation(testFixtures("no.nav.syfo:isyfo-backend-common:$ISYFO_BACKEND_COMMON"))
```

## Available mock classes

All classes are in the `no.nav.syfo.common.mock` package.

### `MockTilgang`

A plain Jackson-serializable data class matching the JSON response from `istilgangskontroll`.

```kotlin
data class MockTilgang(
    val erGodkjent: Boolean,
    val fullTilgang: Boolean = erGodkjent
)
```

Use it in a mock HTTP handler to simulate access granted or denied:

```kotlin
// Access granted (read and write)
respond(MockTilgang(erGodkjent = true, fullTilgang = true))

// Access denied (both populasjonstilgang and fagtilgang denied)
respond(MockTilgang(erGodkjent = false, fullTilgang = false))

// Read access granted, write access denied
respond(MockTilgang(erGodkjent = true, fullTilgang = false))
```

### `MockAzureAdTokenResponse`

A plain Jackson-serializable data class matching the JSON response from the Azure AD token endpoint.

```kotlin
data class MockAzureAdTokenResponse(
    val accessToken: String = "mock-access-token",   // serialized as "access_token"
    val expiresIn: Int = 3600,                       // serialized as "expires_in"
    val tokenType: String = "Bearer"                 // serialized as "token_type"
)
```

Use it in a mock HTTP handler for the Azure AD token endpoint:

```kotlin
respond(MockAzureAdTokenResponse())

// Override the token value if needed
respond(MockAzureAdTokenResponse(accessToken = "my-test-token"))
```
