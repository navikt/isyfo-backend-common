# Token providers

Both clients implement `OboTokenProvider` and `SystemTokenProvider`.

Token methods accept `targetClientId` in `<cluster>.<namespace>.<app>` format — the `api://` prefix and `/.default` suffix are added internally by each client.

---

## EntraIdClient (Texas)

`no.nav.syfo.common.token.texas.EntraIdClient`

Uses the Nais token exchange sidecar (Texas) — no client credentials needed in the constructor. Texas handles caching, renewal, and communication with Entra ID.

### Setup

```kotlin
val entraIdClient = EntraIdClient()
// Reads NAIS_TOKEN_EXCHANGE_ENDPOINT and NAIS_TOKEN_ENDPOINT from environment automatically
```

### On-behalf-of tokens

```kotlin
val token = entraIdClient.getOnBehalfOfToken(
    targetClientId = "<cluster>.<namespace>.<app>", // e.g. "dev-fss.teamsykefravr.istilgangskontroll"
    token = incomingToken
)
```

### System tokens (M2M)

```kotlin
val token = entraIdClient.getSystemToken(
    targetClientId = "<cluster>.<namespace>.<app>"
)
```

### Migrating from AzureAdClient to EntraIdClient (Texas)

- **App code**: Remove the `AzureEnvironment` config and all reading of `AZURE_APP_CLIENT_ID`, `AZURE_APP_CLIENT_SECRET`, `AZURE_APP_WELL_KNOWN_URL`. Replace `AzureAdClient(azureEnvironment)` with `EntraIdClient()` — no arguments needed.
- **Naiserator**: Remove `outbound.external: login.microsoftonline.com` — the app no longer calls Azure AD directly. Keep `azure.application.enabled: true`; the Texas sidecar still uses the app's Azure AD registration and injects `NAIS_TOKEN_EXCHANGE_ENDPOINT` and `NAIS_TOKEN_ENDPOINT` automatically.

---

## AzureAdClient

`no.nav.syfo.common.token.azuread.AzureAdClient`

For apps not yet using the Nais token sidecar (Texas). Communicates directly with Azure AD. System tokens are cached in-memory per scope (refreshed 60 seconds before expiry). OBO tokens are not cached.

### Setup

```kotlin
val azureEnvironment = AzureEnvironment(
    appClientId = "client-id",
    appClientSecret = "client-secret",
    appWellKnownUrl = "https://login.microsoftonline.com/<tenant>/v2.0/.well-known/openid-configuration",
    openidConfigTokenEndpoint = "https://login.microsoftonline.com/<tenant>/oauth2/v2.0/token",
)

val azureAdClient = AzureAdClient(azureEnvironment = azureEnvironment)
```

### On-behalf-of tokens

```kotlin
val token = azureAdClient.getOnBehalfOfToken(
    targetClientId = "<cluster>.<namespace>.<app>", // e.g. "dev-fss.teamsykefravr.istilgangskontroll"
    token = incomingToken,
)
```

### System tokens

```kotlin
val token = azureAdClient.getSystemToken(
    targetClientId = "<cluster>.<namespace>.<app>"
)
```

