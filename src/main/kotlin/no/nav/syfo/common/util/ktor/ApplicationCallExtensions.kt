package no.nav.syfo.common.util.ktor

import com.auth0.jwt.JWT
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import no.nav.syfo.common.person.PersonIdent
import no.nav.syfo.common.util.NAV_CALL_ID_HEADER
import no.nav.syfo.common.util.NAV_PERSONIDENT_HEADER

internal const val JWT_CLAIM_AZP = "azp"
public const val JWT_CLAIM_NAVIDENT: String = "NAVident"

/** Extracts the Bearer token from the `Authorization` header, stripping the `Bearer ` prefix.
 * Returns null if the header is absent. */
public val ApplicationCall.bearerTokenOrNull: String?
    get() = this.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")

/** Extracts the Bearer token from the `Authorization` header, stripping the `Bearer ` prefix.
 * Throws [IllegalArgumentException] if the header is absent. */
public val ApplicationCall.bearerToken: String
    get() = bearerTokenOrNull ?: throw IllegalArgumentException("No Authorization header supplied")

/** Returns the value of the `Nav-Call-Id` request header, or null if not present. */
public val ApplicationCall.callIdOrNull: String?
    get() = this.request.headers[NAV_CALL_ID_HEADER]

/** Returns the value of the `Nav-Call-Id` request header, used for distributed tracing.
 * Throws [IllegalArgumentException] if the header is absent. */
public val ApplicationCall.callId: String
    get() = callIdOrNull ?: throw IllegalArgumentException("No $NAV_CALL_ID_HEADER header supplied")

/** Returns the value of the `nav-personident` request header as a [PersonIdent], or null if not present.
 * The header is used to carry the Norwegian national identity number (fødselsnummer/D-nummer) of the citizen a request
 * concerns.
 * Throws [IllegalArgumentException] if the header value is not a valid 11-digit identity number. */
public val ApplicationCall.personIdentOrNull: PersonIdent?
    get() = this.request.headers[NAV_PERSONIDENT_HEADER]?.let { PersonIdent(it) }

/** Returns the value of the `nav-personident` request header as a [PersonIdent].
 * The header is used to carry the Norwegian national identity number (fødselsnummer/D-nummer) of the citizen a request
 * concerns.
 * Throws [IllegalArgumentException] if the header is absent or if the header value
 * is not a valid 11-digit identity number. */
public val ApplicationCall.personIdent: PersonIdent
    get() = personIdentOrNull ?: throw IllegalArgumentException("No $NAV_PERSONIDENT_HEADER header supplied")

/** Returns the `azp` (authorized party) claim from the incoming Bearer token, or null if absent. */
public fun ApplicationCall.consumerClientIdOrNull(): String? =
    bearerTokenOrNull?.let {
        JWT.decode(it).claims[JWT_CLAIM_AZP]?.asString()
    }

/** Returns the `azp` (authorized party) claim from the incoming Bearer token.
 * Throws [IllegalArgumentException] if there is no Authorization header or if the claim is absent. */
public fun ApplicationCall.consumerClientId(): String =
    consumerClientIdOrNull() ?: throw IllegalArgumentException("Missing $JWT_CLAIM_AZP claim in token")

/** Returns the NAVident (veileder's employee ID) from the `NAVident` private claim in the incoming Bearer token,
 * or null if there is no Authorization header or if the claim is missing. */
public fun ApplicationCall.navIdentOrNull(): String? =
    bearerTokenOrNull?.let {
        JWT.decode(it).claims[JWT_CLAIM_NAVIDENT]?.asString()
    }

/** Returns the NAVident (veileder's employee ID) from the `NAVident` private claim in the incoming Bearer token.
 * Throws [IllegalArgumentException] if there is no Authorization header or if the claim is missing. */
public fun ApplicationCall.navIdent(): String =
    navIdentOrNull() ?: throw IllegalArgumentException("Missing $JWT_CLAIM_NAVIDENT claim in token")
