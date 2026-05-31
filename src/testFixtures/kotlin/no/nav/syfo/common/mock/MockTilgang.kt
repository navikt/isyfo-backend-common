package no.nav.syfo.common.mock

/**
 * Mock response for the istilgangskontroll `erGodkjent` endpoint.
 * Use in test HTTP mock handlers to simulate access granted or denied.
 *
 * [fullTilgang] defaults to the same value as [erGodkjent].
 */
public data class MockTilgang(
    val erGodkjent: Boolean,
    val fullTilgang: Boolean = erGodkjent
)
