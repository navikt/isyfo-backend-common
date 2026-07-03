package no.nav.syfo.common.journalforing

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.syfo.common.http.commonConfig
import no.nav.syfo.common.journalforing.client.DokarkivClient
import no.nav.syfo.common.journalforing.dto.Bruker
import no.nav.syfo.common.journalforing.dto.BrukerIdType
import no.nav.syfo.common.journalforing.dto.JournalpostKanal
import no.nav.syfo.common.journalforing.dto.JournalpostResponse
import no.nav.syfo.common.journalforing.dto.JournalpostType
import no.nav.syfo.common.mock.respond
import no.nav.syfo.common.token.SystemTokenProvider
import no.nav.syfo.common.types.ident.Personident
import no.nav.syfo.common.util.ClientConfig
import no.nav.syfo.common.util.bearerHeader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit test for DokarkivClient. systemTokenProvider and httpClient dependencies are mocked.
 */
class DokarkivClientTest {
    private val systemToken = "system-token"
    private val personident = Personident("12345678910")
    private val clientConfig =
        ClientConfig(
            baseUrl = "dokarkivUrl",
            clientId = "dev-fss.teamdokumenthandtering.dokarkiv",
        )
    private val systemTokenProvider = mockk<SystemTokenProvider>()

    private object TestBrevkode : Brevkode {
        override val value = "OPPF_TEST_BREVKODE"
    }

    private val journalpostId = JournalpostId(1)
    private val journalpostRequest =
        createJournalpostRequest(
            bruker = Bruker(id = personident.value, idType = BrukerIdType.PERSONIDENT.value),
            mottaker = JournalpostMottaker.Person(personident),
            brevkode = TestBrevkode,
            tittel = "Et brev",
            pdf = byteArrayOf(1, 2, 3),
            eksternReferanseId = "ref-1",
            journalpostType = JournalpostType.UTGAAENDE,
            kanal = JournalpostKanal.DITT_NAV,
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
    fun `journalfor returns response and sends expected token, path and forsoekFerdigstill param`() {
        lateinit var authorizationHeader: String
        lateinit var requestPath: String
        var forsoekFerdigstill: String? = null

        val httpClient =
            HttpClient(MockEngine) {
                commonConfig()
                engine {
                    addHandler { request ->
                        authorizationHeader = request.headers[HttpHeaders.Authorization].orEmpty()
                        requestPath = request.url.encodedPath
                        forsoekFerdigstill = request.url.parameters["forsoekFerdigstill"]
                        respond(JournalpostResponse(journalpostId = journalpostId, journalstatus = "FERDIGSTILT"))
                    }
                }
            }

        val client = DokarkivClient(systemTokenProvider, clientConfig, httpClient)

        val response = runBlocking { client.journalfor(journalpostRequest) }

        assertEquals(journalpostId, response.journalpostId)
        assertEquals(bearerHeader(systemToken), authorizationHeader)
        assertTrue(requestPath.endsWith(DokarkivClient.JOURNALPOST_PATH))
        assertEquals("true", forsoekFerdigstill)
    }

    @Test
    fun `journalfor treats 409 Conflict as success and returns existing journalpost`() {
        val existing = JournalpostResponse(journalpostId = journalpostId, journalstatus = "JOURNALFOERT")
        val client = createMockClientForResponse(existing, HttpStatusCode.Conflict)

        val response = runBlocking { client.journalfor(journalpostRequest) }

        assertEquals(journalpostId, response.journalpostId)
    }

    @Test
    fun `journalfor throws on unexpected server error`() {
        val httpClient =
            HttpClient(MockEngine) {
                commonConfig()
                engine {
                    addHandler { respondError(HttpStatusCode.InternalServerError) }
                }
            }
        val client = DokarkivClient(systemTokenProvider, clientConfig, httpClient)

        assertThrows(Exception::class.java) {
            runBlocking { client.journalfor(journalpostRequest) }
        }
    }

    @Test
    fun `journalfor throws when system token request fails`() {
        coEvery { systemTokenProvider.getSystemToken(any()) } returns null
        val client = createMockClientForResponse()

        assertThrows(IllegalStateException::class.java) {
            runBlocking { client.journalfor(journalpostRequest) }
        }
    }

    private fun createMockClientForResponse(
        response: JournalpostResponse = JournalpostResponse(journalpostId = journalpostId, journalstatus = "FERDIGSTILT"),
        status: HttpStatusCode = HttpStatusCode.OK,
    ): DokarkivClient {
        val httpClient =
            HttpClient(MockEngine) {
                commonConfig()
                engine {
                    addHandler { respond(response, status) }
                }
            }
        return DokarkivClient(systemTokenProvider, clientConfig, httpClient)
    }
}
