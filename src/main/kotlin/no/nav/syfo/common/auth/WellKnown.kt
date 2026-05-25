package no.nav.syfo.common.auth

/** OpenID Connect discovery document fields used for JWT validation. */
public data class WellKnown(
    val issuer: String,
    val jwksUri: String
)
