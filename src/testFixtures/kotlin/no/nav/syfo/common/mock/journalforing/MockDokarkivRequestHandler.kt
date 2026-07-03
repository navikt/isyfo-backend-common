package no.nav.syfo.common.mock.journalforing

import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import no.nav.syfo.common.journalforing.client.DokarkivClient.Companion.JOURNALPOST_PATH
import no.nav.syfo.common.mock.respond

/**
 * Mock handler that simulates the `dokarkiv` journalpost endpoint for use in consumers' tests.
 *
 * Responds to [JOURNALPOST_PATH] with a default successful [mockJournalpostResponse]. Pass a
 * custom [responseProvider] to simulate a specific response (e.g. ferdigstilt = false).
 */
public fun MockRequestHandleScope.mockDokarkivRequestHandler(
    request: HttpRequestData,
    responseProvider: () -> Any = { mockJournalpostResponse() },
): HttpResponseData {
    val requestUrl = request.url.encodedPath
    return when {
        requestUrl.endsWith(JOURNALPOST_PATH) -> respond(responseProvider())
        else -> error("Unhandled path $requestUrl")
    }
}
