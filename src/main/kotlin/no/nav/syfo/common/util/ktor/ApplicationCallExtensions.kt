package no.nav.syfo.common.util.ktor

import com.auth0.jwt.JWT
import io.ktor.http.*
import io.ktor.server.application.*
import no.nav.syfo.common.util.NAV_CALL_ID_HEADER
import no.nav.syfo.common.util.NAV_PERSONIDENT_HEADER
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("no.nav.syfo.common.token.texas.TexasHttp")

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

/** Returns the NAVident (user's employee ID) from the `NAVident` private claim in the incoming Bearer token,
 * or null if there is no Authorization header or if the claim is missing. */
public val ApplicationCall.navIdentOrNull: String?
    get() = bearerTokenOrNull?.let {
        JWT.decode(it).claims[JWT_CLAIM_NAVIDENT]?.asString()
    }

/** Returns the NAVident (user's employee ID) from the `NAVident` private claim in the incoming Bearer token.
 * Throws [IllegalArgumentException] if there is no Authorization header or if the claim is missing. */
public val ApplicationCall.navIdent: String
    get() = navIdentOrNull ?: throw IllegalArgumentException("Missing $JWT_CLAIM_NAVIDENT claim in token")

/** Returns the value of the `nav-personident` request header, or null if not present.
 * The header is used to carry the Norwegian national identity number (fødselsnummer/D-nummer)
 * of the citizen a request concerns. */
public val ApplicationCall.personIdentOrNull: String?
    get() = this.request.headers[NAV_PERSONIDENT_HEADER]

/** Returns the value of the `nav-personident` request header.
 * The header is used to carry the Norwegian national identity number (fødselsnummer/D-nummer)
 * of the citizen a request concerns.
 * Throws [IllegalArgumentException] if the header is absent. */
public val ApplicationCall.personIdent: String
    get() = personIdentOrNull ?: throw IllegalArgumentException("No $NAV_PERSONIDENT_HEADER header supplied")

/** Returns the value of the `Nav-Call-Id` request header used for distributed tracing and correlation of log messages
 * across services. If header is missing returns "unknown" and logs a warning.
 * Consider using package io.ktor.server.plugins.callid plugin version instead if app installs it. */
public val ApplicationCall.callId: String
    get() = this.request.headers[NAV_CALL_ID_HEADER] ?: run {
        val appName = System.getenv("NAIS_APP_NAME") ?: "unknown"
        log.warn("Call id missing in request to $appName")
        "unknown"
    }

/** Returns the `azp` (authorized party) claim from the incoming Bearer token.
 * Returns "unknown" and logs a warning if the Authorization header or the claim is absent. */
public val ApplicationCall.consumerClientId: String
    get() = bearerTokenOrNull?.let {
        JWT.decode(it).claims[JWT_CLAIM_AZP]?.asString()
    } ?: run {
        val appName = System.getenv("NAIS_APP_NAME") ?: "unknown"
        log.warn("Missing $JWT_CLAIM_AZP claim in bearer token in request to $appName")
        "unknown"
    }
