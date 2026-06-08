package no.nav.syfo.common.mock.tilgangskontroll

import com.auth0.jwt.JWT
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.common.mock.receiveBody
import no.nav.syfo.common.mock.respond
import no.nav.syfo.common.tilgangskontroll.client.TilgangskontrollClient.Companion.TILGANGSKONTROLL_BRUKERE_PATH
import no.nav.syfo.common.tilgangskontroll.client.TilgangskontrollClient.Companion.TILGANGSKONTROLL_PERSON_PATH
import no.nav.syfo.common.types.ident.NavIdent
import no.nav.syfo.common.types.ident.PersonIdent
import no.nav.syfo.common.util.JWT_CLAIM_NAVIDENT
import no.nav.syfo.common.util.NAV_PERSONIDENT_HEADER

private fun HttpRequestData.navIdentFromToken(): NavIdent? =
    headers[HttpHeaders.Authorization]
        ?.removePrefix("Bearer ")
        ?.let { token ->
            runCatching { JWT.decode(token).claims[JWT_CLAIM_NAVIDENT]?.asString() }
                .getOrNull()
                ?.let { NavIdent(it) }
        }

/**
 * The level of Modia Syfo fagtilgang granted to a mock user.
 */
enum class MockUserSyfoTilgangLevel {
    NONE,
    READ,
    FULL,
}

/**
 * Represents simulated tilgang details for a mock user in tests.
 *
 * @property syfoTilgangLevel The level of Modia Syfo fagtilgang granted to this user.
 *   Use [MockUserSyfoTilgangLevel.NONE] to simulate a user with no Syfo tilgang,
 *   [MockUserSyfoTilgangLevel.READ] to simulate user with Syfo lesetilgang, or
 *   [MockUserSyfoTilgangLevel.FULL] to simulate user with Syfo full tilgang (both read and write).
 * @property personsUserHasAccessTo The set of [PersonIdent]s this user is permitted to access.
 *   Only persons in this set will be returned or approved when access is checked,
 *   provided [syfoTilgangLevel] is not [MockUserSyfoTilgangLevel.NONE].
 */
data class MockUserTilgangDetails(
    val syfoTilgangLevel: MockUserSyfoTilgangLevel,
    val personsUserHasAccessTo: Set<PersonIdent>, // person idents this user has access to
)

/**
 * Mock handler that simulates `istilgangskontroll` service endpoints.
 *
 * Handles the following endpoints:
 * - [TILGANGSKONTROLL_PERSON_PATH] — returns a [MockTilgangResponse] indicating whether the requesting
 *   user has access to a single person and level of Syfo access.
 * - [TILGANGSKONTROLL_BRUKERE_PATH] — filters a list of person idents down to those the user has access to.
 *
 * Access is resolved by extracting the `NAVident` claim from the Bearer token in the request,
 * then looking up the veileder's [MockUserTilgangDetails] in [tilgangDetailsPerNavIdent].
 * If the veileder is unknown or has [MockUserSyfoTilgangLevel.NONE], access is denied.
 */
public fun MockRequestHandleScope.mockTilgangskontrollRequestHandler(
    request: HttpRequestData,
    tilgangDetailsPerNavIdent: Map<NavIdent, MockUserTilgangDetails>,
): HttpResponseData {
    val requestUrl = request.url.encodedPath
    val navIdent = request.navIdentFromToken()
    val userTilgangDetails = navIdent?.let { tilgangDetailsPerNavIdent[it] }

    return when {
        requestUrl.endsWith(TILGANGSKONTROLL_PERSON_PATH) -> {
            val personIdent =
                request.headers[NAV_PERSONIDENT_HEADER]?.let { PersonIdent(it) }
                    ?: return respondError(HttpStatusCode.BadRequest)

            if (userTilgangDetails == null) {
                return respond(MockTilgangResponse(erGodkjent = false, fullTilgang = false))
            }

            val hasSyfoTilgang = userTilgangDetails.syfoTilgangLevel != MockUserSyfoTilgangLevel.NONE
            val hasAccessToPerson = personIdent in userTilgangDetails.personsUserHasAccessTo

            respond(
                MockTilgangResponse(
                    erGodkjent = hasSyfoTilgang && hasAccessToPerson,
                    fullTilgang = userTilgangDetails.syfoTilgangLevel == MockUserSyfoTilgangLevel.FULL,
                ),
            )
        }

        requestUrl.endsWith(TILGANGSKONTROLL_BRUKERE_PATH) -> {
            val personIdentsToFilter =
                runBlocking<List<String>> { request.receiveBody() }.toList().map { PersonIdent(it) }

            if (userTilgangDetails == null || userTilgangDetails.syfoTilgangLevel == MockUserSyfoTilgangLevel.NONE) {
                return respond(emptyList<String>())
            }

            val filteredPersonsUserHasAccessTo =
                personIdentsToFilter.filter { it in userTilgangDetails.personsUserHasAccessTo }

            respond(filteredPersonsUserHasAccessTo.map { it.value })
        }

        else -> error("Unhandled path $requestUrl")
    }
}
