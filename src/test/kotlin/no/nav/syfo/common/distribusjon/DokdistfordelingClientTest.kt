package no.nav.syfo.common.distribusjon

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.common.distribusjon.client.DokdistfordelingClient
import no.nav.syfo.common.distribusjon.dto.DistribuerJournalpostResponse
import no.nav.syfo.common.http.commonConfig
import no.nav.syfo.common.mock.respond
import no.nav.syfo.common.token.SystemTokenProvider
import no.nav.syfo.common.util.ClientConfig
import no.nav.syfo.common.util.bearerHeader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit test for DokdistfordelingClient. systemTokenProvider and httpClient dependencies are mocked.
 */
class DokdistfordelingClientTest {
    private val systemToken = "system-token"
    private val clientConfig =
        ClientConfig(
            baseUrl = "dokdistfordelingUrl",
            clientId = "dev-fss.teamdokumenthandtering.dokdistfordeling",
        )
    private val systemTokenProvider = mockk<SystemTokenProvider>()

    private val request =
        createDistribuerJournalpostRequest(
            journalpostId = "123",
            bestillendeFagsystem = "ISYFO",
        )

    @BeforeEach
    fun setup() {
        coEvery { systemTokenProvider.getSystemToken(any()) } returns systemToken
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `distribuer returns response and sends expected token and path`() {
        lateinit var authorizationHeader: String
        lateinit var requestPath: String

        val httpClient =
            HttpClient(MockEngine) {
                commonConfig()
                engine {
                    addHandler { request ->
                        authorizationHeader = request.headers[HttpHeaders.Authorization].orEmpty()
                        requestPath = request.url.encodedPath
                        respond(DistribuerJournalpostResponse(bestillingsId = "bestilling-1"))
                    }
                }
            }

        val client = DokdistfordelingClient(systemTokenProvider, clientConfig, httpClient)

        val response = runBlocking { client.distribuer(request) }

        assertEquals("bestilling-1", response.bestillingsId)
        assertEquals(bearerHeader(systemToken), authorizationHeader)
        assertTrue(requestPath.endsWith(DokdistfordelingClient.DISTRIBUER_PATH))
    }

    @Test
    fun `distribuer treats 409 Conflict as success and returns existing bestillingsId`() {
        val existing = DistribuerJournalpostResponse(bestillingsId = "bestilling-existing")
        val client = createMockClientForResponse(existing, HttpStatusCode.Conflict)

        val response = runBlocking { client.distribuer(request) }

        assertEquals("bestilling-existing", response.bestillingsId)
    }

    @Test
    fun `distribuer throws on unexpected server error`() {
        val httpClient =
            HttpClient(MockEngine) {
                commonConfig()
                engine {
                    addHandler { respondError(HttpStatusCode.InternalServerError) }
                }
            }
        val client = DokdistfordelingClient(systemTokenProvider, clientConfig, httpClient)

        assertThrows(Exception::class.java) {
            runBlocking { client.distribuer(request) }
        }
    }

    @Test
    fun `distribuer throws when system token request fails`() {
        coEvery { systemTokenProvider.getSystemToken(any()) } returns null
        val client = createMockClientForResponse()

        assertThrows(IllegalStateException::class.java) {
            runBlocking { client.distribuer(request) }
        }
    }

    private fun createMockClientForResponse(
        response: DistribuerJournalpostResponse = DistribuerJournalpostResponse(bestillingsId = "bestilling-1"),
        status: HttpStatusCode = HttpStatusCode.OK,
    ): DokdistfordelingClient {
        val httpClient =
            HttpClient(MockEngine) {
                commonConfig()
                engine {
                    addHandler { respond(response, status) }
                }
            }
        return DokdistfordelingClient(systemTokenProvider, clientConfig, httpClient)
    }
}
