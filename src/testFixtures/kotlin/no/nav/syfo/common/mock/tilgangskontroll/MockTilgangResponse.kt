package no.nav.syfo.common.mock.tilgangskontroll

/**
 * Mock response for the istilgangskontroll `erGodkjent` endpoint.
 * Use in test HTTP mock handlers to simulate access granted or denied.
 */
public data class MockTilgangResponse(
    val erGodkjent: Boolean,
    val fullTilgang: Boolean,
)
