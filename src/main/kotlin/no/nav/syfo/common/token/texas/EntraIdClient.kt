package no.nav.syfo.common.token.texas

import io.ktor.client.HttpClient
import no.nav.syfo.common.http.defaultHttpClient
import no.nav.syfo.common.token.OboTokenProvider
import no.nav.syfo.common.token.SystemTokenProvider

/**
 * Client for obtaining tokens from the Entra ID identity provider (formerly Azure AD)
 * via the Nais token exchange sidecar (Texas).
 *
 * Supports both exchanging employee tokens for OBO-tokens ([OboTokenProvider]) for consuming an API on behalf of an
 * employee, and obtaining system tokens ([SystemTokenProvider]) for consuming an API as the application itself.
 *
 * Reads `NAIS_TOKEN_EXCHANGE_ENDPOINT` and `NAIS_TOKEN_ENDPOINT` from the environment automatically.
 *
 * @see <a href="https://docs.nais.io/auth/entra-id/">Nais Entra ID documentation</a>
 */
public class EntraIdClient(
    private val httpClient: HttpClient = defaultHttpClient()
) : OboTokenProvider, SystemTokenProvider {

    override suspend fun getOnBehalfOfToken(targetClientId: String, token: String): String? =
        texasOboToken(httpClient, TexasIdentityProvider.ENTRA_ID, targetClientId, token)

    override suspend fun getSystemToken(targetClientId: String): String? =
        texasSystemToken(httpClient, TexasIdentityProvider.ENTRA_ID, targetClientId)
}
