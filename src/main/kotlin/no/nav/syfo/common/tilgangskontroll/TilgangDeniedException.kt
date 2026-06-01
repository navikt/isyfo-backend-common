package no.nav.syfo.common.tilgangskontroll

/**
 * Thrown when the user is denied access by istilgangskontroll.
 * Typically caught by a Ktor status page handler and mapped to a 403 Forbidden response.
 */
public class TilgangDeniedException(
    action: String,
    message: String = "Denied NAVIdent access to personident: $action"
) : RuntimeException(message)
