package no.nav.syfo.common.token.azuread

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.accept
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import no.nav.syfo.common.http.proxyHttpClient
import no.nav.syfo.common.token.OboTokenProvider
import no.nav.syfo.common.token.SystemTokenProvider
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Client for obtaining tokens from Entra ID (formerly Azure AD) identity provider directly (not using Texas).
 *
 * Supports both exchanging employee tokens for OBO-tokens ([OboTokenProvider]) for consuming an API on behalf of an
 * employee, and obtaining system tokens ([SystemTokenProvider]) for consuming an API as the application itself.
 *
 * Reads configuration from [AzureAdClientConfig], which defaults to reading NAIS-injected
 * environment variables via [AzureAdClientConfig.fromEnv].
 *
 * @param config Azure AD configuration. Defaults to [AzureAdClientConfig.fromEnv].
 * @param httpClient Optional HTTP client override; defaults to a proxy-aware client.
 */
public class AzureAdClient(
    private val config: AzureAdClientConfig = AzureAdClientConfig.fromEnv(),
    private val httpClient: HttpClient = proxyHttpClient()
) : OboTokenProvider, SystemTokenProvider {
    /**
     * Exchanges the caller's token for an on-behalf-of token scoped to [targetClientId].
     *
     * @param targetClientId The client ID of the downstream service to request a token for, in `cluster.namespace.app` format.
     * @param token The caller's bearer token to exchange.
     * @return The raw access token string, or `null` if the exchange failed.
     */
    override suspend fun getOnBehalfOfToken(targetClientId: String, token: String): String? = getAccessToken(
        Parameters.build {
            append("client_id", config.appClientId)
            append("client_secret", config.appClientSecret)
            append("client_assertion_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
            append("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
            append("assertion", token)
            append("scope", "api://$targetClientId/.default")
            append("requested_token_use", "on_behalf_of")
        }
    )?.toAzureAdToken()?.accessToken

    /**
     * Acquires a machine-to-machine token scoped to [targetClientId] using client credentials.
     *
     * Tokens are cached in memory and reused until expiry to avoid unnecessary requests.
     *
     * @param targetClientId The client ID of the downstream service to request a token for, in `cluster.namespace.app` format.
     * @return The raw access token string, or `null` if acquisition failed.
     */
    public override suspend fun getSystemToken(targetClientId: String): String? {
        val cacheKey = "${CACHE_AZUREAD_TOKEN_SYSTEM_KEY_PREFIX}$targetClientId"
        val cachedToken = cache.get(key = cacheKey)
        return (
            if (cachedToken?.isExpired() == false) {
                COUNT_CALL_AZUREAD_SYSTEM_TOKEN_CACHE_HIT.increment()
                cachedToken
            } else {
                COUNT_CALL_AZUREAD_SYSTEM_TOKEN_CACHE_MISS.increment()
                val azureAdTokenResponse = getAccessToken(
                    Parameters.build {
                        append("client_id", config.appClientId)
                        append("client_secret", config.appClientSecret)
                        append("grant_type", "client_credentials")
                        append("scope", "api://$targetClientId/.default")
                    }
                )
                azureAdTokenResponse?.let { token ->
                    token.toAzureAdToken().also {
                        cache[cacheKey] = it
                    }
                }
            }
            )?.accessToken
    }

    private suspend fun getAccessToken(
        formParameters: Parameters
    ): AzureAdTokenResponse? =
        try {
            val response: HttpResponse = httpClient.post(config.openidConfigTokenEndpoint) {
                accept(ContentType.Application.Json)
                setBody(FormDataContent(formParameters))
            }
            response.body<AzureAdTokenResponse>()
        } catch (e: ClientRequestException) {
            log.error(
                "Client error while requesting AzureAD access token with statusCode=${e.response.status.value}",
                e
            )
            null
        } catch (e: ServerResponseException) {
            log.error(
                "Server error while requesting AzureAD access token with statusCode=${e.response.status.value}",
                e
            )
            null
        }

    private companion object {
        private const val CACHE_AZUREAD_TOKEN_SYSTEM_KEY_PREFIX = "azuread-token-system-"
        private val cache = ConcurrentHashMap<String, AzureAdToken>()
        private val log = LoggerFactory.getLogger(AzureAdClient::class.java)
    }
}
