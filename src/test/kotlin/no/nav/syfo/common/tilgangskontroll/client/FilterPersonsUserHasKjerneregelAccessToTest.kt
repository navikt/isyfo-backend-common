package no.nav.syfo.common.tilgangskontroll.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.common.http.commonConfig
import no.nav.syfo.common.mock.receiveBody
import no.nav.syfo.common.mock.respond
import no.nav.syfo.common.token.OboTokenProvider
import no.nav.syfo.common.types.ident.Personident
import no.nav.syfo.common.util.ClientConfig
import no.nav.syfo.common.util.NAV_CALL_ID_HEADER
import no.nav.syfo.common.util.bearerHeader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit test for the `filterPersonsUserHasKjerneregelAccessTo()` method in TilgangskontrollClient.
 * Tests the method with various scenarios including successful responses, access denied,
 * server errors, and empty lists. Mirrors [FilterPersonsUserHasAccessToTest].
 */
class FilterPersonsUserHasKjerneregelAccessToTest {
    private val token = "token"
    private val oboToken = "obo-token"
    private val callId = "call-id"
    private val personidenter =
        listOf(Personident("12345678910"), Personident("10987654321"), Personident("11223344556"))
    private val config =
        ClientConfig(
            baseUrl = "isTilgangskontrollUrl",
            clientId = "dev-fss.teamsykefravr.istilgangskontroll",
        )
    private val oboTokenProvider = mockk<OboTokenProvider>()

    @BeforeEach
    fun setup() {
        coEvery {
            oboTokenProvider.getOnBehalfOfToken(any(), any())
        } returns oboToken
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `filterPersonsUserHasKjerneregelAccessTo calls expected url with expected headers and body`() {
        lateinit var requestUrl: String
        lateinit var authorizationHeader: String
        lateinit var callIdHeader: String
        lateinit var requestBody: List<String>

        val httpClient =
            HttpClient(MockEngine) {
                commonConfig()
                engine {
                    addHandler { request ->
                        requestUrl = request.url.toString()
                        authorizationHeader = request.headers[HttpHeaders.Authorization].orEmpty()
                        callIdHeader = request.headers[NAV_CALL_ID_HEADER].orEmpty()
                        requestBody = runBlocking { request.receiveBody<List<String>>() }
                        respond(personidenter, HttpStatusCode.OK)
                    }
                }
            }

        val client =
            TilgangskontrollClient(
                oboTokenProvider = oboTokenProvider,
                clientConfig = config,
                httpClient = httpClient,
            )

        runBlocking {
            client.filterPersonsUserHasKjerneregelAccessTo(
                personidenter = personidenter,
                token = token,
                callId = callId,
            )
        }

        assertTrue(requestUrl.endsWith(TilgangskontrollClient.TILGANGSKONTROLL_BRUKERE_KJERNEREGLER_PATH))
        assertEquals(bearerHeader(oboToken), authorizationHeader)
        assertEquals(callId, callIdHeader)
        assertEquals(personidenter.map { it.value }, requestBody)
    }

    @Test
    fun `filterPersonsUserHasKjerneregelAccessTo returns filtered list when access is granted`() {
        val filteredPersonidenter = listOf(Personident("12345678910"), Personident("10987654321"))
        val client = createMockTilgangskontrollClientForFilterPersonsResponse(filteredPersonidenter, HttpStatusCode.OK)

        val result =
            runBlocking {
                client.filterPersonsUserHasKjerneregelAccessTo(
                    personidenter = personidenter,
                    token = token,
                    callId = callId,
                )
            }

        assertEquals(filteredPersonidenter, result)
    }

    @Test
    fun `filterPersonsUserHasKjerneregelAccessTo returns full list when all persons are accessible`() {
        val client = createMockTilgangskontrollClientForFilterPersonsResponse(personidenter, HttpStatusCode.OK)

        val result =
            runBlocking {
                client.filterPersonsUserHasKjerneregelAccessTo(
                    personidenter = personidenter,
                    token = token,
                    callId = callId,
                )
            }

        assertEquals(personidenter, result)
    }

    @Test
    fun `filterPersonsUserHasKjerneregelAccessTo returns empty list when no persons are accessible`() {
        val client = createMockTilgangskontrollClientForFilterPersonsResponse(emptyList(), HttpStatusCode.OK)

        val result =
            runBlocking {
                client.filterPersonsUserHasKjerneregelAccessTo(
                    personidenter = personidenter,
                    token = token,
                    callId = callId,
                )
            }

        assertEquals(emptyList<Personident>(), result)
    }

    @Test
    fun `filterPersonsUserHasKjerneregelAccessTo returns null when access is forbidden (403)`() {
        val client = createMockTilgangskontrollClientForFilterPersonsResponse(null, HttpStatusCode.Forbidden)

        val result =
            runBlocking {
                client.filterPersonsUserHasKjerneregelAccessTo(
                    personidenter = personidenter,
                    token = token,
                    callId = callId,
                )
            }

        assertNull(result)
    }

    @Test
    fun `filterPersonsUserHasKjerneregelAccessTo returns null when server returns error (500)`() {
        val client = createMockTilgangskontrollClientForFilterPersonsResponse(null, HttpStatusCode.InternalServerError)

        val result =
            runBlocking {
                client.filterPersonsUserHasKjerneregelAccessTo(
                    personidenter = personidenter,
                    token = token,
                    callId = callId,
                )
            }

        assertNull(result)
    }

    @Test
    fun `filterPersonsUserHasKjerneregelAccessTo calls obo token exchange before making request`() {
        val client = createMockTilgangskontrollClientForFilterPersonsResponse(personidenter, HttpStatusCode.OK)

        runBlocking {
            client.filterPersonsUserHasKjerneregelAccessTo(
                personidenter = personidenter,
                token = token,
                callId = callId,
            )
        }

        coVerify {
            oboTokenProvider.getOnBehalfOfToken(
                targetClientId = config.clientId,
                token = token,
            )
        }
    }

    @Test
    fun `filterPersonsUserHasKjerneregelAccessTo throws when obo token request fails`() {
        coEvery {
            oboTokenProvider.getOnBehalfOfToken(any(), any())
        } returns null

        val client = createMockTilgangskontrollClientForFilterPersonsResponse(personidenter, HttpStatusCode.OK)

        val exception =
            assertThrows(RuntimeException::class.java) {
                runBlocking {
                    client.filterPersonsUserHasKjerneregelAccessTo(
                        personidenter = personidenter,
                        token = token,
                        callId = callId,
                    )
                }
            }

        assertTrue(exception.message?.contains("Failed to request access to list of persons") ?: false)
    }

    private fun createMockTilgangskontrollClientForFilterPersonsResponse(
        response: List<Personident>?,
        status: HttpStatusCode,
    ): TilgangskontrollClient {
        val httpClient =
            HttpClient(MockEngine) {
                commonConfig()
                engine {
                    addHandler {
                        if (status == HttpStatusCode.OK && response != null) {
                            respond(response, status)
                        } else {
                            respondError(status)
                        }
                    }
                }
            }

        return TilgangskontrollClient(
            oboTokenProvider = oboTokenProvider,
            clientConfig = config,
            httpClient = httpClient,
        )
    }
}
