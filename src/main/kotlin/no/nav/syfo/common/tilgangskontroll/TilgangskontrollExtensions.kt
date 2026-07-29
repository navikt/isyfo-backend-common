package no.nav.syfo.common.tilgangskontroll

import io.ktor.server.routing.RoutingContext
import no.nav.syfo.common.tilgangskontroll.client.TilgangskontrollClient
import no.nav.syfo.common.types.ident.Personident
import no.nav.syfo.common.util.bearerTokenOrThrow
import no.nav.syfo.common.util.callIdOrGenerate
import no.nav.syfo.common.util.navidentOrThrow
import no.nav.syfo.common.util.personidentOrThrow

/**
 * [RoutingContext] tilgangskontroll helper that checks both that the user has populasjonstilgang to a specific person,
 * and that the user has the necessary Modia SYFO fagtilgang, and executes wrapped code if checks pass.
 * Reads the `nav-personident` header from the request to get the id number of the citizen to check access to.
 *
 * Throws [TilgangDeniedException] if access is denied.
 * Expects `nav-personident` header, otherwise throws [IllegalArgumentException].
 * Expects user bearer token on authorization header, otherwise throws [IllegalArgumentException].
 * Tries to read call id from call id header, but does not throw if it's missing.
 *
 * @param action Short description of the action being performed, used in error messages.
 * @param tilgangskontrollClient Configured [TilgangskontrollClient].
 * @param requiresWriteAccess If true, checks for write access level according to the user's Modia SYFO fagtilgang.
 *                            If false, checks for at least read access level.
 * @param block The handler to execute if access is granted.
 *              Receives the [AuthorizedUser] (with token and lazy navident),
 *              the [Personident] for the target person that the user has been granted access to, and the callId.
 */
public suspend fun RoutingContext.checkPersonAndSyfoTilgang(
    action: String,
    tilgangskontrollClient: TilgangskontrollClient,
    requiresWriteAccess: Boolean = false,
    block: suspend (AuthorizedUser, Personident, String) -> Unit,
) {
    val personident = call.personidentOrThrow(action)

    checkPersonAndSyfoTilgang(
        action = action,
        personident = personident,
        tilgangskontrollClient = tilgangskontrollClient,
        requiresWriteAccess = requiresWriteAccess,
        block = block,
    )
}

/**
 * [RoutingContext] tilgangskontroll helper that checks both that the user has populasjonstilgang to a specific person,
 * and that the user has the necessary Modia SYFO fag-tilgang, and executes wrapped code if checks pass.
 *
 * Use this overload when the personident comes from the request body rather than the `nav-personident` header.
 *
 * Throws [TilgangDeniedException] if denied.
 * Expects user bearer token on authorization header, otherwise throws [IllegalArgumentException].
 * Expects call id from call id header, but does not throw if it's missing.
 *
 * @param action Short description of the action being performed, used in error messages.
 * @param personident The [Personident] of the person to check access for.
 * @param tilgangskontrollClient Configured [TilgangskontrollClient] used to check access.
 * @param requiresWriteAccess If true, checks for write access level according to the user's Modia SYFO fagtilgang.
 *                            If false, checks for at least read access level.
 * @param block The handler to execute if access is granted.
 *              Receives the [AuthorizedUser] (with token and lazy navident),
 *              the [Personident] for the target person that the user has been granted access to, and the callId.
 */
public suspend fun RoutingContext.checkPersonAndSyfoTilgang(
    action: String,
    personident: Personident,
    tilgangskontrollClient: TilgangskontrollClient,
    requiresWriteAccess: Boolean = false,
    block: suspend (AuthorizedUser, Personident, String) -> Unit,
) {
    val token = call.bearerTokenOrThrow(action)
    val callId = call.callIdOrGenerate()
    val navBruker =
        AuthorizedUser(
            token = token,
            navidentProvider = { call.navidentOrThrow(action) },
        )

    val hasAccess =
        if (requiresWriteAccess) {
            tilgangskontrollClient.hasWriteAccess(
                callId = callId,
                personident = personident,
                token = token,
            )
        } else {
            tilgangskontrollClient.hasAccess(
                callId = callId,
                personident = personident,
                token = token,
            )
        }

    if (!hasAccess) {
        throw TilgangDeniedException(
            action = action,
            navident = navBruker.navident.value,
        )
    } else {
        block(
            navBruker,
            personident,
            callId,
        )
    }
}

/**
 * [RoutingContext] tilgangskontroll helper to get the subset of a list of [personidenter] that the user has access to.
 * Returns null on error or if `istilagngskontroll` responds with status forbidden, and returns and empty
 * list if user has access to none of the persons or if user does not have at least read access per Syfo Modia
 * fagtilgang.
 *
 * @param action Short description of the action being performed, used in error messages.
 * @param personidenter List of national identity numbers (fødselsnummer) to check if user has access to.
 * @param tilgangskontrollClient Configured [TilgangskontrollClient] used to check access.
 */
public suspend fun RoutingContext.filterPersonsUserHasAccessTo(
    action: String,
    personidenter: List<Personident>,
    tilgangskontrollClient: TilgangskontrollClient,
): List<Personident>? {
    val token = call.bearerTokenOrThrow(action)
    val callId = call.callIdOrGenerate()

    val personsUserHasAccessTo = tilgangskontrollClient.filterPersonsUserHasAccessTo(personidenter, token, callId)

    return personsUserHasAccessTo
}
