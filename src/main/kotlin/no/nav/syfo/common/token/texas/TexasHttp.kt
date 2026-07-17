package no.nav.syfo.common.token.texas

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import no.nav.syfo.common.util.getRequiredEnvVar
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("no.nav.syfo.common.token.texas")

private val tokenExchangeEndpoint: String get() = getRequiredEnvVar("NAIS_TOKEN_EXCHANGE_ENDPOINT")
private val tokenEndpoint: String get() = getRequiredEnvVar("NAIS_TOKEN_ENDPOINT")

internal suspend fun texasOboToken(
    httpClient: HttpClient,
    identityProvider: TexasIdentityProvider,
    targetClientId: String,
    token: String,
): String? =
    try {
        val response =
            httpClient.post(tokenExchangeEndpoint) {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(
                    TexasTokenExchangeRequest(
                        identity_provider = identityProvider.value,
                        target = identityProvider.formatTarget(targetClientId),
                        user_token = token,
                    ),
                )
            }
        response.body<TexasTokenResponse>().access_token
    } catch (e: ClientRequestException) {
        log.error(
            "Client error while exchanging OBO token via Texas (${identityProvider.value}): statusCode=${e.response.status.value}",
            e,
        )
        null
    } catch (e: ServerResponseException) {
        log.error(
            "Server error while exchanging OBO token via Texas (${identityProvider.value}): statusCode=${e.response.status.value}",
            e,
        )
        null
    }

internal suspend fun texasSystemToken(
    httpClient: HttpClient,
    identityProvider: TexasIdentityProvider,
    targetClientId: String,
): String? =
    try {
        val response =
            httpClient.post(tokenEndpoint) {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
                setBody(
                    TexasTokenRequest(
                        identity_provider = identityProvider.value,
                        target = identityProvider.formatTarget(targetClientId),
                    ),
                )
            }
        response.body<TexasTokenResponse>().access_token
    } catch (e: ClientRequestException) {
        log.error(
            "Client error while acquiring system token via Texas (${identityProvider.value}): statusCode=${e.response.status.value}",
            e,
        )
        null
    } catch (e: ServerResponseException) {
        log.error(
            "Server error while acquiring system token via Texas (${identityProvider.value}): statusCode=${e.response.status.value}",
            e,
        )
        null
    }
