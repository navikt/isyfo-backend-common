package no.nav.syfo.common.distribusjon.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.syfo.common.distribusjon.dto.DistribuerJournalpostRequest
import no.nav.syfo.common.distribusjon.dto.DistribuerJournalpostResponse
import no.nav.syfo.common.http.defaultHttpClient
import no.nav.syfo.common.token.SystemTokenProvider
import no.nav.syfo.common.util.ClientConfig
import no.nav.syfo.common.util.bearerHeader
import org.slf4j.LoggerFactory

/**
 * Client for `dokdistfordeling` — the felles tjeneste for distribusjon av journalposter.
 *
 * Orders distribution of a previously journalført document to its recipient (digitally or on
 * paper, per dokdistfordeling's own channel selection unless overridden).
 *
 * Uses a [SystemTokenProvider] to obtain a system token scoped to dokdistfordeling before each
 * call — distribution is ordered as the application itself, not on behalf of a user.
 *
 * Build the [DistribuerJournalpostRequest] with
 * [no.nav.syfo.common.distribusjon.createDistribuerJournalpostRequest] for the common case.
 *
 * @param systemTokenProvider Supplies system tokens. Pass an
 *  [no.nav.syfo.common.token.azuread.AzureAdClient] or
 *  [no.nav.syfo.common.token.texas.EntraIdClient] directly, or a custom lambda.
 * @param clientConfig [ClientConfig] for dokdistfordeling (base URL + client id).
 * @param httpClient HTTP client to use. Defaults to [defaultHttpClient]. Override in tests with a mock engine.
 */
public class DokdistfordelingClient(
    private val systemTokenProvider: SystemTokenProvider,
    private val clientConfig: ClientConfig,
    private val httpClient: HttpClient = defaultHttpClient(),
) {
    private val distribuerUrl = "${clientConfig.baseUrl}$DISTRIBUER_PATH"

    /**
     * Orders distribution of a journalpost via dokdistfordeling.
     *
     * A 409 Conflict (the journalpost has already been distributed) is treated as success and the
     * existing [DistribuerJournalpostResponse] is returned.
     *
     * @throws ClientRequestException for unexpected 4xx responses (other than 409 Conflict).
     * @throws ServerResponseException for 5xx responses.
     * @throws IllegalStateException if no system token could be obtained.
     */
    public suspend fun distribuer(request: DistribuerJournalpostRequest): DistribuerJournalpostResponse {
        val systemToken =
            systemTokenProvider.getSystemToken(clientConfig.clientId)
                ?: error(
                    "Failed to distribuer: Failed to get system token for dokdistfordeling from " +
                        systemTokenProvider::class.simpleName,
                )

        return try {
            val response =
                httpClient.post(distribuerUrl) {
                    header(HttpHeaders.Authorization, bearerHeader(systemToken))
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            COUNT_CALL_DOKDISTFORDELING_DISTRIBUER_SUCCESS.increment()
            response.body<DistribuerJournalpostResponse>()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Conflict) {
                val distribuerResponse = e.response.body<DistribuerJournalpostResponse>()
                log.warn(
                    "Journalpost with id {} was already distributed in dokdistfordeling (409 Conflict)",
                    request.journalpostId,
                )
                COUNT_CALL_DOKDISTFORDELING_DISTRIBUER_CONFLICT.increment()
                distribuerResponse
            } else {
                handleUnexpectedResponseException(e.response)
                throw e
            }
        } catch (e: ServerResponseException) {
            handleUnexpectedResponseException(e.response)
            throw e
        }
    }

    private fun handleUnexpectedResponseException(response: HttpResponse) {
        log.error(
            "Error while requesting dokdistfordeling to distribuer journalpost: statusCode={}",
            response.status.value,
        )
        COUNT_CALL_DOKDISTFORDELING_DISTRIBUER_FAIL.increment()
    }

    public companion object {
        private val log = LoggerFactory.getLogger(DokdistfordelingClient::class.java)

        public const val DISTRIBUER_PATH: String = "/rest/v1/distribuerjournalpost"
    }
}
