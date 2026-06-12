package no.nav.syfo.common.mock.token.azuread

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import no.nav.syfo.common.mock.respond

/**
 * Mock handler that simulates Azure AD service obo token endpoint by responding with the same token back,
 * so that the token can be read out again in mock request handlers for other services that are called with an obo token,
 * like [mockTilgangskontrollRequestHandler][no.nav.syfo.common.mock.tilgangskontroll.mockTilgangskontrollRequestHandler].
 * These mock handlers can use the token to read the NAV-ident claim and simulate different behavior like different
 * tilgang for different mock users / navidents.
 *
 * This will respond with the incoming "assertion token" as accessToken for getOnBehalfOfToken calls,
 * and with "token" as the accessToken for getSystemToken calls.
 */
public fun MockRequestHandleScope.mockAzureAdRequestHandler(request: HttpRequestData): HttpResponseData {
    // The token to exchange is sent to AzureAd service from AzureAdClient via an assertion form parameter
    val assertionToken = (request.body as? FormDataContent)?.formData?.get("assertion")

    return respond(
        MockAzureAdTokenResponse(
            accessToken = assertionToken ?: "token",
            expiresIn = 3600,
            tokenType = "type",
        ),
    )
}
