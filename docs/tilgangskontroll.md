# Veileder tilgangskontroll

Client and Ktor `RoutingContext` helpers for checking veileder access via `istilgangskontroll`.

## Setup

```kotlin
val tilgangskontrollClient = TilgangskontrollClient(
    oboTokenProvider = EntraIdClient(), // or AzureAdClient(...)
    clientConfig = ClientConfig(
        baseUrl = "https://istilgangskontroll",
        clientId = "dev-fss.teamsykefravr.istilgangskontroll",
    ),
)
```

## Ktor route helpers

In route handlers, it is easiest to check tilgangskontroll through the `RoutingContext` extension helpers. They extract the token and call id from the request, call `istilgangskontroll`, and throw `TilgangDeniedException` if access is denied (in apps we can translate this to a `403 Forbidden` response).

### `checkPersonAndSyfoTilgang`

Checks that the user has populasjonstilgang to a specific person and the required Modia SYFO fagtilgang. If the checks pass, the `block` handler is executed with the authorized user (`AuthorizedUser`, carrying the token and a lazily-resolved `navIdent`), the validated `personIdent`, and the `callId`.

One overload reads `nav-personident` from the request header:

```kotlin
get("/person") {
    checkPersonAndSyfoTilgang(
        action = "read person",
        tilgangskontrollClient = tilgangskontrollClient,
        requiresWriteAccess = false, // false by default
    ) { authorizedUser, targetPersonIdent, callId ->
        callOtherService(authorizedUser.token, callId)
        
        getThing(authorizedUser.navIdent, targetPersonIdent)
        
        call.respond(HttpStatusCode.OK)
    }
}
```

Another overload takes `personIdent` as an explicit `PersonIdent` parameter (e.g. when read from the request body):

```kotlin
post("/person") {
    val requestDTO = call.receive<RequestDTO>()
    
    checkPersonAndSyfoTilgang(
        action = "write person",
        personIdent = PersonIdent(requestDTO.personIdent),
        tilgangskontrollClient = tilgangskontrollClient,
        requiresWriteAccess = true,
    ) { authorizedUser, targetPersonIdent ->
        createThing(authorizedUser.navIdent, targetPersonIdent)
        
        call.respond(HttpStatusCode.Created)
    }
}
```

Based on whether `requiresWriteAccess` is true or false, it will check that the user has a Modia Syfo fagtilgang
giving that level of access to Modia SYFO. 

Required request headers:
- `Authorization: Bearer <token>`
- `nav-personident` (if not providing personIdent as argument)
- Will try to read `Nav-Call-Id` header, but will not throw if it's missing.

### `filterPersonsUserHasAccessTo`

Returns the subset of a list of persons that the veileder has access to. Returns `null` on error or if `istilgangskontroll` responds with `403 Forbidden`, and an empty list if the veileder has access to none of the persons.

```kotlin
get("/persons") {
    val accessiblePersonIdenter = filterPersonsUserHasAccessTo(
        action = "filter persons",
        personIdenter = listOf("12345678910", "10987654321"),
        tilgangskontrollClient = tilgangskontrollClient,
    )
    call.respond(accessiblePersonIdenter ?: emptyList())
}
```

## Access semantics

- Read access (`requiresWriteAccess = false`, default): granted when `erGodkjent == true`
- Write access (`requiresWriteAccess = true`): granted when `erGodkjent == true && fullTilgang == true`
- A `403 Forbidden` from `istilgangskontroll` is treated as access denied (not an error)

## Direct client usage

If you need to check access outside of a Ktor route handler, use `TilgangskontrollClient` directly:

```kotlin
val hasReadAccess = tilgangskontrollClient.hasAccess(
    callId = "call-id",
    personIdent = PersonIdent("12345678910"),
    token = incomingToken,
)

val hasWriteAccess = tilgangskontrollClient.hasWriteAccess(
    callId = "call-id",
    personIdent = PersonIdent("12345678910"),
    token = incomingToken,
)

val accessiblePersonIdenter: List<PersonIdent>? = tilgangskontrollClient.filterPersonsUserHasAccessTo(
    personIdenter = listOf(PersonIdent("12345678910"), PersonIdent("10987654321")),
    token = incomingToken,
    callId = "call-id",
)
```
