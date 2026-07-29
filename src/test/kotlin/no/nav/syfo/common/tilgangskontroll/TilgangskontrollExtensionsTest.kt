package no.nav.syfo.common.tilgangskontroll

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.RoutingRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.registerInstanceFactory
import kotlinx.coroutines.runBlocking
import no.nav.syfo.common.testutil.jwtWithNavident
import no.nav.syfo.common.tilgangskontroll.client.TilgangskontrollClient
import no.nav.syfo.common.types.ident.Personident
import no.nav.syfo.common.util.NAV_CALL_ID_HEADER
import no.nav.syfo.common.util.NAV_PERSONIDENT_HEADER
import no.nav.syfo.common.util.bearerHeader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TilgangskontrollExtensionsTest {
    private val action = "read aktivitetskrav"
    private val callId = "123"
    private val personident = Personident("12345678910")
    private val token = jwtWithNavident("Z123456")

    private val tilgangskontrollClient = mockk<TilgangskontrollClient>()

    @BeforeEach
    fun setup() {
        registerInstanceFactory { personident }
    }

    @Test
    fun `calls hasAccess and executes block when read access is granted`() {
        val routingContext =
            routingContextWithHeaders(
                headers =
                    Headers.build {
                        append(NAV_CALL_ID_HEADER, callId)
                        append(NAV_PERSONIDENT_HEADER, personident.value)
                        append(HttpHeaders.Authorization, bearerHeader(token))
                    },
            )
        var blockCalled = false
        var blockToken: String? = null
        var blockPersonident: Personident? = null
        var blockCallId: String? = null

        coEvery {
            tilgangskontrollClient.hasAccess(callId, personident, token)
        } returns true

        runBlocking {
            routingContext.checkPersonAndSyfoTilgang(
                action = action,
                tilgangskontrollClient = tilgangskontrollClient,
            ) { authorized, targetPersonident, blockCallIdArg ->
                blockCalled = true
                blockToken = authorized.token
                blockPersonident = targetPersonident
                blockCallId = blockCallIdArg
            }
        }

        Assertions.assertTrue(blockCalled)
        Assertions.assertEquals(token, blockToken)
        Assertions.assertEquals(personident, blockPersonident)
        Assertions.assertEquals(callId, blockCallId)
        coVerify(exactly = 1) {
            tilgangskontrollClient.hasAccess(callId, personident, token)
        }
        coVerify(exactly = 0) {
            tilgangskontrollClient.hasWriteAccess(any(), any(), any())
        }
    }

    @Test
    fun `calls hasWriteAccess and executes block when write access is granted`() {
        val routingContext =
            routingContextWithHeaders(
                headers =
                    Headers.build {
                        append(NAV_CALL_ID_HEADER, callId)
                        append(NAV_PERSONIDENT_HEADER, personident.value)
                        append(HttpHeaders.Authorization, bearerHeader(token))
                    },
            )
        var blockCalled = false

        coEvery {
            tilgangskontrollClient.hasWriteAccess(callId, personident, token)
        } returns true

        runBlocking {
            routingContext.checkPersonAndSyfoTilgang(
                action = action,
                tilgangskontrollClient = tilgangskontrollClient,
                requiresWriteAccess = true,
            ) { _, _, _ ->
                blockCalled = true
            }
        }

        Assertions.assertTrue(blockCalled)
        coVerify(exactly = 1) {
            tilgangskontrollClient.hasWriteAccess(callId, personident, token)
        }
        coVerify(exactly = 0) {
            tilgangskontrollClient.hasAccess(any(), any(), any())
        }
    }

    @Test
    fun `throws forbidden and does not execute block when read access is denied`() {
        val routingContext =
            routingContextWithHeaders(
                headers =
                    Headers.build {
                        append(NAV_CALL_ID_HEADER, callId)
                        append(NAV_PERSONIDENT_HEADER, personident.value)
                        append(HttpHeaders.Authorization, bearerHeader(token))
                    },
            )
        var blockCalled = false

        coEvery {
            tilgangskontrollClient.hasAccess(callId, personident, token)
        } returns false

        Assertions.assertThrows(TilgangDeniedException::class.java) {
            runBlocking {
                routingContext.checkPersonAndSyfoTilgang(
                    action = action,
                    tilgangskontrollClient = tilgangskontrollClient,
                ) { _, _, _ ->
                    blockCalled = true
                }
            }
        }

        Assertions.assertFalse(blockCalled)
        coVerify(exactly = 1) {
            tilgangskontrollClient.hasAccess(callId, personident, token)
        }
    }

    @Test
    fun `throws illegal argument when authorization header is missing`() {
        val routingContext =
            routingContextWithHeaders(
                headers =
                    Headers.build {
                        append(NAV_CALL_ID_HEADER, callId)
                        append(NAV_PERSONIDENT_HEADER, personident.value)
                    },
            )

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                routingContext.checkPersonAndSyfoTilgang(
                    action = action,
                    tilgangskontrollClient = tilgangskontrollClient,
                ) { _, _, _ -> }
            }
        }

        coVerify(exactly = 0) {
            tilgangskontrollClient.hasAccess(any(), any(), any())
        }
        coVerify(exactly = 0) {
            tilgangskontrollClient.hasWriteAccess(any(), any(), any())
        }
    }

    @Test
    fun `throws illegal argument when personident header is missing`() {
        val routingContext =
            routingContextWithHeaders(
                headers =
                    Headers.build {
                        append(NAV_CALL_ID_HEADER, callId)
                        append(HttpHeaders.Authorization, bearerHeader(token))
                    },
            )

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                routingContext.checkPersonAndSyfoTilgang(
                    action = action,
                    tilgangskontrollClient = tilgangskontrollClient,
                ) { _, _, _ -> }
            }
        }

        coVerify(exactly = 0) {
            tilgangskontrollClient.hasAccess(any(), any(), any())
        }
        coVerify(exactly = 0) {
            tilgangskontrollClient.hasWriteAccess(any(), any(), any())
        }
    }

    @Test
    fun `explicit personident - calls hasAccess and executes block when read access is granted`() {
        val routingContext =
            routingContextWithHeaders(
                headers =
                    Headers.build {
                        append(NAV_CALL_ID_HEADER, callId)
                        append(HttpHeaders.Authorization, bearerHeader(token))
                    },
            )
        var blockCalled = false

        coEvery {
            tilgangskontrollClient.hasAccess(callId, personident, token)
        } returns true

        runBlocking {
            routingContext.checkPersonAndSyfoTilgang(
                action = action,
                personident = personident,
                tilgangskontrollClient = tilgangskontrollClient,
            ) { _, _, _ ->
                blockCalled = true
            }
        }

        Assertions.assertTrue(blockCalled)
        coVerify(exactly = 1) {
            tilgangskontrollClient.hasAccess(callId, personident, token)
        }
    }

    @Test
    fun `explicit personident - calls hasWriteAccess and executes block when write access is granted`() {
        val routingContext =
            routingContextWithHeaders(
                headers =
                    Headers.build {
                        append(NAV_CALL_ID_HEADER, callId)
                        append(HttpHeaders.Authorization, bearerHeader(token))
                    },
            )
        var blockCalled = false

        coEvery {
            tilgangskontrollClient.hasWriteAccess(callId, personident, token)
        } returns true

        runBlocking {
            routingContext.checkPersonAndSyfoTilgang(
                action = action,
                personident = personident,
                tilgangskontrollClient = tilgangskontrollClient,
                requiresWriteAccess = true,
            ) { _, _, _ ->
                blockCalled = true
            }
        }

        Assertions.assertTrue(blockCalled)
        coVerify(exactly = 1) {
            tilgangskontrollClient.hasWriteAccess(callId, personident, token)
        }
    }

    @Test
    fun `explicit personident - throws forbidden when access is denied`() {
        val routingContext =
            routingContextWithHeaders(
                headers =
                    Headers.build {
                        append(NAV_CALL_ID_HEADER, callId)
                        append(HttpHeaders.Authorization, bearerHeader(token))
                    },
            )
        var blockCalled = false

        coEvery {
            tilgangskontrollClient.hasAccess(callId, personident, token)
        } returns false

        Assertions.assertThrows(TilgangDeniedException::class.java) {
            runBlocking {
                routingContext.checkPersonAndSyfoTilgang(
                    action = action,
                    personident = personident,
                    tilgangskontrollClient = tilgangskontrollClient,
                ) { _, _, _ ->
                    blockCalled = true
                }
            }
        }

        Assertions.assertFalse(blockCalled)
    }

    @Test
    fun `explicit personident - throws illegal argument when authorization header is missing`() {
        val routingContext =
            routingContextWithHeaders(
                headers =
                    Headers.build {
                        append(NAV_CALL_ID_HEADER, callId)
                    },
            )

        Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                routingContext.checkPersonAndSyfoTilgang(
                    action = action,
                    personident = personident,
                    tilgangskontrollClient = tilgangskontrollClient,
                ) { _, _, _ -> }
            }
        }

        coVerify(exactly = 0) {
            tilgangskontrollClient.hasAccess(any(), any(), any())
        }
    }

    private fun routingContextWithHeaders(headers: Headers): RoutingContext {
        val routingRequest = mockk<RoutingRequest>()
        every { routingRequest.headers } returns headers

        val routingCall = mockk<RoutingCall>()
        every { routingCall.request } returns routingRequest

        val routingContext = mockk<RoutingContext>()
        every { routingContext.call } returns routingCall
        return routingContext
    }
}
