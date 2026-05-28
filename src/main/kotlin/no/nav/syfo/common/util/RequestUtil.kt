package no.nav.syfo.common.util

/**
 * Correlation/tracing header name for header passed between Nav services
 * so a single user request can be traced across multiple microservices in
 * logs and monitoring tools.
 */
public const val NAV_CALL_ID_HEADER: String = "Nav-Call-Id"

/**
 * Conventional name of header used to carry the Norwegian national
 * identity number (fødselsnummer/D-nummer) of the citizen a request
 * concerns, from the frontend/API gateway to backend services.
 */
public const val NAV_PERSONIDENT_HEADER: String = "nav-personident"

/**
 * Builds a Bearer token authorization header value.
 */
public fun bearerHeader(token: String): String = "Bearer $token"
