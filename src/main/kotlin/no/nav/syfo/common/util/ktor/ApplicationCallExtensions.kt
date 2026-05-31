package no.nav.syfo.common.util.ktor

import com.auth0.jwt.JWT
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import no.nav.syfo.common.person.PersonIdent
import no.nav.syfo.common.util.NAV_CALL_ID_HEADER
import no.nav.syfo.common.util.NAV_PERSONIDENT_HEADER

internal const val JWT_CLAIM_AZP = "azp"
public const val JWT_CLAIM_NAVIDENT: String = "NAVident"

/** Returns the value of the `Nav-Call-Id` request header, used for distributed tracing. */
public fun ApplicationCall.getCallId(): String = this.request.headers[NAV_CALL_ID_HEADER].toString()

/** Returns the value of the `nav-personident` request header as a [PersonIdent], or null if not present.
 * Throws [IllegalArgumentException] if the header value is not a valid 11-digit identity number. */
public fun ApplicationCall.getPersonIdent(): PersonIdent? =
    this.request.headers[NAV_PERSONIDENT_HEADER]?.let { PersonIdent(it) }

/**
 * Returns the `azp` (authorized party) claim from the incoming Bearer token, identifying the calling application.
 * Returns null if there is no Authorization header or if the claim is absent.
 */
public fun ApplicationCall.getConsumerClientId(): String? =
    getBearerToken()?.let {
        JWT.decode(it).claims[JWT_CLAIM_AZP]?.asString()
    }

/**
 * Extracts the NAVident (veileder's employee ID) from the `NAVident` private claim in the incoming Bearer token.
 * Throws [Error] if there is no Authorization header or if the claim is missing.
 */
public fun ApplicationCall.getNavIdent(): String {
    val token = getBearerToken() ?: throw Error("No Authorization header supplied")
    return JWT.decode(token).claims[JWT_CLAIM_NAVIDENT]?.asString()
        ?: throw Error("Missing NAVident in private claims")
}

/**
 * Extracts the Bearer token from the `Authorization` header, stripping the `Bearer ` prefix.
 * Returns null if the header is absent.
 */
public fun ApplicationCall.getBearerToken(): String? =
    this.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
