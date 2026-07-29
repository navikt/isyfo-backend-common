package no.nav.syfo.common.tilgangskontroll

/**
 * Thrown when the user is denied access by istilgangskontroll.
 * Typically caught by a Ktor status page handler and mapped to a 403 Forbidden response.
 */
public class TilgangDeniedException(
    action: String,
    navident: String = "[unknown]",
    message: String = "Failed to $action: Access denied – User with ident $navident does not have required persontilgang or fagtilgang.",
) : RuntimeException(message)
