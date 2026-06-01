package no.nav.syfo.common.tilgangskontroll.ktor

import io.ktor.server.routing.RoutingContext
import no.nav.syfo.common.person.PersonIdent
import no.nav.syfo.common.tilgangskontroll.VeilederTilgangForbiddenException
import no.nav.syfo.common.tilgangskontroll.client.TilgangskontrollClient
import no.nav.syfo.common.util.ktor.bearerToken
import no.nav.syfo.common.util.ktor.callId
import no.nav.syfo.common.util.ktor.personIdent

/**
 * ktor [RoutingContext] convenience helper for using [TilgangskontrollClient] for access control. Checks both that the
 * user has populasjonstilgang to a specific citizen, and that the user has the necessary Modia SYFO fag-tilgang, and
 * executes wrapped code if checks pass.
 * Reads the `nav-Personident` header from the request to get the id number of the citizen to check access to, so
 * this overload requires that header to be present.
 *
 * Throws [VeilederTilgangForbiddenException] if denied.
 *
 * @param action Short description of the action being performed, used in error messages.
 * @param tilgangskontrollClient Configured [TilgangskontrollClient] used to check access.
 * @param requiresWriteAccess If true, checks for fag-tilgangen fullTilgang (write access) rather than fagtilgangen for
 *                            read access.
 * @param block The handler to execute if access is granted.
 */
public suspend fun RoutingContext.checkPersonAndSyfoTilgang(
    action: String,
    tilgangskontrollClient: TilgangskontrollClient,
    requiresWriteAccess: Boolean = false,
    block: suspend () -> Unit
) {
    val personIdent = call.personIdent

    checkPersonAndSyfoTilgang(
        action = action,
        personIdent = personIdent,
        tilgangskontrollClient = tilgangskontrollClient,
        requiresWriteAccess = requiresWriteAccess,
        block = block
    )
}

/**
 * ktor [RoutingContext] convenience helper for using [TilgangskontrollClient] for access control. Checks both that the
 * user has populasjonstilgang to a specific citizen, and that the user has the necessary Modia SYFO fag-tilgang, and
 * executes wrapped code if checks pass.
 *
 * Use this overload when the personIdent comes from the request body rather than the `nav-personident` header.
 *
 * Throws [VeilederTilgangForbiddenException] if denied.
 *
 * @param action Short description of the action being performed, used in error messages.
 * @param personIdent The person's national identity number to check access for.
 * @param tilgangskontrollClient Configured [TilgangskontrollClient] used to check access.
 * @param requiresWriteAccess If true, checks for fullTilgang (write access) rather than read access.
 * @param block The handler to execute if access is granted.
 */
public suspend fun RoutingContext.checkPersonAndSyfoTilgang(
    action: String,
    personIdent: PersonIdent,
    tilgangskontrollClient: TilgangskontrollClient,
    requiresWriteAccess: Boolean = false,
    block: suspend () -> Unit
) {
    val callId = call.callId
    val token = call.bearerToken

    val hasAccess = if (requiresWriteAccess) {
        tilgangskontrollClient.hasWriteAccess(
            callId = callId,
            personIdent = personIdent,
            token = token
        )
    } else {
        tilgangskontrollClient.hasAccess(
            callId = callId,
            personIdent = personIdent,
            token = token
        )
    }

    if (!hasAccess) {
        throw VeilederTilgangForbiddenException(action = action)
    } else {
        block()
    }
}
