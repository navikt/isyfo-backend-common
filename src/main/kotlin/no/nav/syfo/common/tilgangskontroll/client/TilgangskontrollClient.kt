package no.nav.syfo.common.tilgangskontroll.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.syfo.common.http.defaultHttpClient
import no.nav.syfo.common.token.OboTokenProvider
import no.nav.syfo.common.types.ident.Personident
import no.nav.syfo.common.util.ClientConfig
import no.nav.syfo.common.util.NAV_CALL_ID_HEADER
import no.nav.syfo.common.util.NAV_PERSONIDENT_HEADER
import no.nav.syfo.common.util.bearerHeader
import org.slf4j.LoggerFactory

/**
 * Client for `istilgangskontroll` — the isyfo service that checks if a veileder or other user has access to a given
 * citizen person, and what level of access the user has to Modia Syfo given their Modia Syfo fagtilgang.
 *
 * Uses an [OboTokenProvider] to exchange the user's incoming token for an OBO token scoped to istilgangskontroll
 * before making requests.
 *
 * @param oboTokenProvider Supplies OBO tokens for the user's token. Pass an [no.nav.syfo.common.token.azuread.AzureAdClient]
 * directly, or wrap a custom token source in a lambda: `{ scopeClientId, token -> ... }`.
 * @param clientConfig [ClientConfig] for istilgangskontroll.
 * @param httpClient HTTP client to use. Defaults to [defaultHttpClient]. Override in tests with a mock engine.
 */
public class TilgangskontrollClient(
    private val oboTokenProvider: OboTokenProvider,
    private val clientConfig: ClientConfig,
    private val httpClient: HttpClient = defaultHttpClient(),
) {
    private val tilgangskontrollPersonUrl = "${clientConfig.baseUrl}$TILGANGSKONTROLL_PERSON_PATH"
    private val tilgangskontrollBrukereUrl = "${clientConfig.baseUrl}$TILGANGSKONTROLL_BRUKERE_PATH"

    private suspend fun getTilgang(
        callId: String,
        personident: Personident,
        token: String,
    ): Tilgang? {
        val oboToken =
            oboTokenProvider.getOnBehalfOfToken(
                targetClientId = clientConfig.clientId,
                token = token,
            )
                ?: error(
                    "Failed to get tiltang for user: Failed to get OBO token for istilgangskontroll from " +
                        oboTokenProvider::class.simpleName,
                )

        return try {
            val tilgangResponse =
                httpClient.get(tilgangskontrollPersonUrl) {
                    header(HttpHeaders.Authorization, bearerHeader(oboToken))
                    header(NAV_PERSONIDENT_HEADER, personident.value)
                    header(NAV_CALL_ID_HEADER, callId)
                    accept(ContentType.Application.Json)
                }
            COUNT_CALL_TILGANGSKONTROLL_PERSON_SUCCESS.increment()
            tilgangResponse.body<Tilgang>()
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.Forbidden) {
                COUNT_CALL_TILGANGSKONTROLL_PERSON_FORBIDDEN.increment()
            } else {
                handleUnexpectedResponseException(e.response, callId)
                COUNT_CALL_TILGANGSKONTROLL_PERSON_FAIL.increment()
            }
            null
        }
    }

    private fun handleUnexpectedResponseException(
        response: HttpResponse,
        callId: String,
    ) {
        log.error(
            "Error while requesting access to person from istilgangskontroll: statusCode={}, callId={}",
            response.status.value,
            callId,
        )
    }

    /**
     * Returns true if the user has access to the given person per populasjonstilgang, and the user has at least
     * read access given the user's Modia Syfo fagtilgang.
     *
     * @param callId Forwarded to istilgangskontroll as the `Nav-Call-Id` request header for tracing across services.
     * @param personident The person's national identity number (fødselsnummer).
     * @param token The user's incoming Bearer token (without the "Bearer " prefix).
     */
    public suspend fun hasAccess(
        callId: String,
        personident: Personident,
        token: String,
    ): Boolean = getTilgang(callId, personident, token)?.erGodkjent ?: false

    /**
     * Returns true if the user has access to the given person per populasjonstilgang, and the user has
     * write access (fullTilgang) given the user's Modia Syfo fagtilgang.
     *
     * Returns false if the user does not have access to the person, or if the user does not have write access.
     *
     * @param callId Forwarded to istilgangskontroll as the `Nav-Call-Id` request header for tracing across services.
     * @param personident The national identity number (fødselsnummer) of person to check if user has access to.
     * @param token The user's incoming Bearer token (without the "Bearer " prefix).
     */
    public suspend fun hasWriteAccess(
        callId: String,
        personident: Personident,
        token: String,
    ): Boolean =
        getTilgang(callId, personident, token)?.let {
            it.erGodkjent && it.fullTilgang
        } ?: false

    /**
     * Returns the subset of a list of [personidenter] that the user has access to.
     * Returns null on error or if istilagngskontroll responds with status forbidden, and returns and empty
     * list if user has access to none of the persons or if user does not have at least read access per Syfo Modia
     * fagtilgang.
     *
     * @param personidenter List of national identity numbers (fødselsnummer) to check if user has access to.
     * @param token The user's incoming Bearer token (without the "Bearer " prefix).
     */
    public suspend fun filterPersonsUserHasAccessTo(
        personidenter: List<Personident>,
        token: String,
        callId: String,
    ): List<Personident>? {
        val oboToken =
            oboTokenProvider.getOnBehalfOfToken(
                targetClientId = clientConfig.clientId,
                token = token,
            ) ?: throw RuntimeException("Failed to request access to list of persons: Failed to get OBO token")

        return try {
            val response: HttpResponse =
                httpClient.post(tilgangskontrollBrukereUrl) {
                    header(HttpHeaders.Authorization, bearerHeader(oboToken))
                    header(NAV_CALL_ID_HEADER, callId)
                    accept(ContentType.Application.Json)
                    header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(personidenter.map { it.value })
                }
            response.body<List<String>>().map { Personident(it) }
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.Forbidden) {
                log.warn("Forbidden to request access to list of person from istilgangskontroll")
                null
            } else {
                log.error("Error while requesting access to list of person from istilgangskontroll: ${e.message}", e)
                null
            }
        } catch (e: ServerResponseException) {
            log.error("Error while requesting access to list of person from istilgangskontroll: ${e.message}", e)
            null
        }
    }

    public companion object {
        private val log = LoggerFactory.getLogger(TilgangskontrollClient::class.java)

        public const val TILGANGSKONTROLL_PERSON_PATH: String = "/api/tilgang/navident/person"
        public const val TILGANGSKONTROLL_BRUKERE_PATH: String = "/api/tilgang/navident/brukere"
    }
}
