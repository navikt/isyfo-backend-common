# Dokarkiv journalføring

`DokarkivClient` and helpers for journalføring (archiving) documents to `dokarkiv` (Joark), shared across iSyfo backend apps. Supports recipients of type **person** (FNR), **virksomhet** (ORGNR) and **behandler** (HPR), and lets each app supply its own set of brevkoder.

## Setup

```kotlin
val dokarkivClient = DokarkivClient(
    systemTokenProvider = EntraIdClient(), // or AzureAdClient(...)
    clientConfig = ClientConfig(
        baseUrl = "https://dokarkiv",
        clientId = "dev-fss.teamdokumenthandtering.dokarkiv",
    ),
)
```

Journalføring is performed as the application itself, so a `SystemTokenProvider` is used (not OBO).

## Brevkoder

Brevkoder are app-specific, so the library does not ship a fixed set. Each app implements the `Brevkode` interface — typically as an enum:

```kotlin
enum class MyBrevkode(override val value: String) : Brevkode {
    FORHANDSVARSEL("OPPF_MIN_APP_FORHANDSVARSEL"),
    VEDTAK("OPPF_MIN_APP_VEDTAK"),
}
```

## Journalføre a document

Build the request with `createJournalpostRequest` and pass it to `journalfor`:

```kotlin
val request = createJournalpostRequest(
    bruker = Bruker("12345678910", BrukerIdType.PERSONIDENT.value),
    mottaker = JournalpostMottaker.Person(
        personident = Personident("12345678910"),
        navn = "Ola Nordmann",
    ),
    brevkode = MyBrevkode.VEDTAK,
    tittel = "Vedtak om noe",
    pdf = pdfBytes,                                       // PDF/A
    eksternReferanseId = vedtakUuid.toString(),           // idempotency key
    kanal = JournalpostKanal.DITT_NAV,
)

val response: JournalpostResponse = dokarkivClient.journalfor(request)
```

### Recipient (mottaker) types

```kotlin
JournalpostMottaker.Person(personident = Personident("12345678910"), navn = "Ola Nordmann")
JournalpostMottaker.Virksomhet(virksomhetsnummer = Virksomhetsnummer("123456789"), navn = "Bedrift AS")
JournalpostMottaker.Behandler(hprId = 12345, navn = "Lege Legesen") // hprId is left-padded to 9 digits
```

## Result and error semantics

- On success, `journalfor` returns the `JournalpostResponse` from dokarkiv.
- A `409 Conflict` (a journalpost with the same `eksternReferanseId` already exists) is treated as **success**: the existing `JournalpostResponse` is returned. This makes retries idempotent.
- Any other unexpected `4xx`/`5xx` response is **thrown** (`ClientRequestException` / `ServerResponseException`).
- If no system token can be obtained, an `IllegalStateException` is thrown.

## Parameters and invariants

The DTOs validate dokarkiv's invariants in their `init` blocks (throwing `IllegalArgumentException`), whether you build them via `createJournalpostRequest` or construct them directly.

### `journalpostType`

- `INNGAAENDE` — documentation Nav has **received** from an external party (søknader, ettersendelser, meldinger fra arbeidsgivere).
- `UTGAAENDE` — documentation Nav has **produced and sent** to an external party (informasjons-/vedtaksbrev to persons or organisations).
- `NOTAT` — documentation Nav produced for itself, not meant to be distributed out of Nav (forvaltningsnotater, telefonnotater).

### `avsenderMottaker`

- Required for `INNGAAENDE` (the avsender) and `UTGAAENDE` (the mottaker); **must not be set** for `NOTAT`.
- `id` and `idType` must both be set, or both be null.
- Digit count is validated per `idType`: `FNR` = 11, `ORGNR` = 9, `HPRNR` = 7-9 digits. The builder left-pads behandler HPR numbers to 9 digits.
- `navn` (format `Fornavn Mellomnavn Etternavn`) is optional; for `FNR` and `ORGNR` dokarkiv resolves the name from PDL/ereg.

### `kanal`

Optional. `NAV_NO` (Ditt Nav — submitted digitally via self-service on nav.no) or `HELSENETTET` (received via Norsk Helsenett).

### `overstyrInnsynsregler`

Optional. Makes the journalpost and its documents visible to the logged-in citizen on nav.no even when the default rule set would hide them:

- `VISES_MASKINELT_GODKJENT` — a machine process approved showing it.
- `VISES_MANUELT_GODKJENT` — a Nav employee reviewed and approved showing it.

### `sak`

- `GENERELL_SAK` (default) — for documents not belonging to a concrete fagsak.
- `FAGSAK` — documents belong to a sak in a fagsystem; both `fagsakId` and `fagsaksystem` are then required.

## Low-level DTOs

For non-standard needs (e.g. multiple documents, custom `Sak`, `overstyrInnsynsregler`), construct a `JournalpostRequest` directly using the public DTOs in `no.nav.syfo.common.journalforing.dto` (`AvsenderMottaker`, `Bruker`, `Dokument`, `DokumentInfo`, `Dokumentvariant`, `Sak`, etc.) and pass it to `journalfor`.

## Testing

Test fixtures provide a mock handler for consumers' tests:

```kotlin
val httpClient = HttpClient(MockEngine) {
    commonConfig()
    engine {
        addHandler { request -> mockDokarkivRequestHandler(request) }
    }
}
```

`mockDokarkivRequestHandler` responds with a default successful `mockJournalpostResponse()`; pass a custom `responseProvider` to simulate other responses.
