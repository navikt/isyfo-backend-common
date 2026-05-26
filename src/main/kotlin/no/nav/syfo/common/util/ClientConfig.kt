package no.nav.syfo.common.util

/**
 * Configuration for a secured internal downstream service / API.
 * Bundles together everything needed to both call the service and
 * authenticate the call.
 *
 * @param baseUrl The base URL of the downstream service, used to
 *  construct HTTP request URLs.
 * @param clientId Used by identity providers (like Entra ID)
 *  when requesting a token scoped to the service.
 */
public data class ClientConfig(
    val baseUrl: String,
    val clientId: String
)

/**
 * Configuration for an internal downstream service / API that does not
 * require token authentication.
 *
 * @param baseUrl The base URL of the downstream service, used to
 *  construct HTTP request URLs.
 */
public data class OpenClientConfig(
    val baseUrl: String
)
