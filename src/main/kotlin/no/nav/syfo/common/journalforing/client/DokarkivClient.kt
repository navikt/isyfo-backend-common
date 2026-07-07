package no.nav.syfo.common.journalforing.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.syfo.common.http.defaultHttpClient
import no.nav.syfo.common.journalforing.dto.JournalpostRequest
import no.nav.syfo.common.journalforing.dto.JournalpostResponse
import no.nav.syfo.common.token.SystemTokenProvider
import no.nav.syfo.common.util.ClientConfig
import no.nav.syfo.common.util.bearerHeader
import org.slf4j.LoggerFactory

/**
 * Client for `dokarkiv` (Joark) — the felles journalføring/archiving service.
 *
 * Creates a journalpost (archives one or more documents) for a citizen, with the request
 * `forsoekFerdigstill=true` so dokarkiv attempts to finalise it immediately.
 *
 * Uses a [SystemTokenProvider] to obtain a system token scoped to dokarkiv before each call —
 * journalføring is performed as the application itself, not on behalf of a user.
 *
 * Build the [JournalpostRequest] with [no.nav.syfo.common.journalforing.createJournalpostRequest] for the common case.
 *
 * @param systemTokenProvider Supplies system tokens. Pass an
 *  [no.nav.syfo.common.token.azuread.AzureAdClient] or
 *  [no.nav.syfo.common.token.texas.EntraIdClient] directly, or a custom lambda.
 * @param clientConfig [ClientConfig] for dokarkiv (base URL + client id).
 * @param httpClient HTTP client to use. Defaults to [defaultHttpClient]. Override in tests with a mock engine.
 */
public class DokarkivClient(
    private val systemTokenProvider: SystemTokenProvider,
    private val clientConfig: ClientConfig,
    private val httpClient: HttpClient = defaultHttpClient(),
) {
    private val journalpostUrl = "${clientConfig.baseUrl}$JOURNALPOST_PATH"

    /**
     * Creates a journalpost in dokarkiv.
     *
     * A 409 Conflict (the journalpost already exists for the given `eksternReferanseId`) is
     * treated as success and the existing [JournalpostResponse] is returned.
     *
     * @throws ClientRequestException for unexpected 4xx responses (other than 409 Conflict).
     * @throws ServerResponseException for 5xx responses.
     * @throws IllegalStateException if no system token could be obtained.
     */
    public suspend fun journalfor(journalpostRequest: JournalpostRequest): JournalpostResponse {
        val systemToken =
            systemTokenProvider.getSystemToken(clientConfig.clientId)
                ?: error(
                    "Failed to journalfor: Failed to get system token for dokarkiv from " +
                        systemTokenProvider::class.simpleName,
                )

        return try {
            val response =
                httpClient.post(journalpostUrl) {
                    parameter(JOURNALPOST_PARAM_FORSOEK_FERDIGSTILL, true)
                    header(HttpHeaders.Authorization, bearerHeader(systemToken))
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    setBody(journalpostRequest)
                }
            COUNT_CALL_DOKARKIV_JOURNALPOST_SUCCESS.increment()
            response.body<JournalpostResponse>()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Conflict) {
                val journalpostResponse = e.response.body<JournalpostResponse>()
                log.warn(
                    "Journalpost with id {} already existed in dokarkiv (409 Conflict)",
                    journalpostResponse.journalpostId,
                )
                COUNT_CALL_DOKARKIV_JOURNALPOST_CONFLICT.increment()
                journalpostResponse
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
            "Error while requesting dokarkiv to journalfor document: statusCode={}",
            response.status.value,
        )
        COUNT_CALL_DOKARKIV_JOURNALPOST_FAIL.increment()
    }

    public companion object {
        private val log = LoggerFactory.getLogger(DokarkivClient::class.java)

        public const val JOURNALPOST_PATH: String = "/rest/journalpostapi/v1/journalpost"
        private const val JOURNALPOST_PARAM_FORSOEK_FERDIGSTILL = "forsoekFerdigstill"
    }
}
