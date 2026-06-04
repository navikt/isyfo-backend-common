package no.nav.syfo.common.util

import com.auth0.jwt.JWT
import io.ktor.http.*
import io.ktor.server.application.*
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("no.nav.syfo.common.util")

internal const val JWT_CLAIM_AZP = "azp"
public const val JWT_CLAIM_NAVIDENT: String = "NAVident"

/** Extracts the Bearer token from the `Authorization` header, stripping the `Bearer ` prefix.
 * Returns null if the header is absent. */
public val ApplicationCall.bearerToken: String?
    get() = this.request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")

/** Extracts the Bearer token from the `Authorization` header, stripping the `Bearer ` prefix.
 * @throws [IllegalArgumentException] if the header is absent. */
public fun ApplicationCall.bearerTokenOrThrow(): String = requireNotNull(bearerToken) { "Authorization header is missing." }

/** Extracts the Bearer token from the `Authorization` header, stripping the `Bearer ` prefix.
 * @param action Short description of the action being performed, used in error messages.
 * @throws [IllegalArgumentException] if the header is absent. */
public fun ApplicationCall.bearerTokenOrThrow(action: String): String =
    requireNotNull(bearerToken) { "Failed to $action: Authorization header is missing." }

/** Returns the NAVident (user's employee ID) from the `NAVident` private claim in the incoming Bearer token,
 * or null if there is no Authorization header or if the claim is missing. */
public val ApplicationCall.navIdent: String?
    get() =
        bearerToken?.let {
            JWT.decode(it).claims[JWT_CLAIM_NAVIDENT]?.asString()
        }

/** Returns the NAVident (user's employee ID) from the `NAVident` private claim in the incoming Bearer token.
 * @throws [IllegalArgumentException] if there is no Authorization header or if the claim is missing. */
public fun ApplicationCall.navIdentOrThrow(): String = requireNotNull(navIdent) { "Missing token or $JWT_CLAIM_NAVIDENT claim in token." }

/** Returns the NAVident (user's employee ID) from the `NAVident` private claim in the incoming Bearer token.
 * @param action Short description of the action being performed, used in error messages.
 * @throws [IllegalArgumentException] if there is no Authorization header or if the claim is missing. */
public fun ApplicationCall.navIdentOrThrow(action: String): String =
    requireNotNull(navIdent) { "Failed to $action: Missing token or $JWT_CLAIM_NAVIDENT claim in token." }

/** Returns the value of the `nav-personident` request header, or null if not present.
 * The header is used to carry the Norwegian national identity number (fødselsnummer/D-nummer)
 * of the citizen a request concerns. */
public val ApplicationCall.personIdent: String?
    get() = this.request.headers[NAV_PERSONIDENT_HEADER]

/** Returns the value of the `nav-personident` request header.
 * The header is used to carry the Norwegian national identity number (fødselsnummer/D-nummer)
 * of the citizen a request concerns.
 * @throws [IllegalArgumentException] if the header is absent. */
public fun ApplicationCall.personIdentOrThrow(): String = requireNotNull(personIdent) { "No $NAV_PERSONIDENT_HEADER header supplied." }

/** Returns the value of the `nav-personident` request header.
 * The header is used to carry the Norwegian national identity number (fødselsnummer/D-nummer)
 * of the citizen a request concerns.
 * @param action Short description of the action being performed, used in error messages.
 * @throws [IllegalArgumentException] if the header is absent. */
public fun ApplicationCall.personIdentOrThrow(action: String): String =
    requireNotNull(personIdent) { "Failed to $action: No $NAV_PERSONIDENT_HEADER header supplied." }

/** Returns the value of the `Nav-Call-Id` request header used for distributed tracing and correlation of log messages
 * across services. If header is missing returns null and logs a warning.
 * Consider using package io.ktor.server.plugins.callid plugin version instead if app installs it. */
public fun ApplicationCall.callIdOrGenerate(): String =
    this.request.headers[NAV_CALL_ID_HEADER]
        ?: generateCallId().also { log.warn("Call id header missing in request, generated new call id.") }

/** Returns the `azp` (authorized party) claim from the incoming Bearer token.
 * Returns null and logs a warning if the Authorization header or the claim is absent. */
public val ApplicationCall.consumerClientId: String?
    get() =
        bearerToken?.let {
            JWT.decode(it).claims[JWT_CLAIM_AZP]?.asString()
        } ?: null.also { log.warn("Missing Authorization header or $JWT_CLAIM_AZP claim in bearer token.") }
