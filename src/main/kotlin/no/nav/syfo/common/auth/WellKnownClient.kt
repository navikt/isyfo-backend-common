package no.nav.syfo.common.auth

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import no.nav.syfo.common.http.proxyHttpClient
import no.nav.syfo.common.util.getRequiredEnvVar

/**
 * Fetches and parses the OpenID Connect discovery document from [wellKnownUrl].
 * Uses a proxy-aware HTTP client. Intended to be called once at application startup.
 */
public fun getWellKnown(wellKnownUrl: String): WellKnown =
    runBlocking {
        proxyHttpClient().use { client ->
            client.get(wellKnownUrl).body<WellKnownDTO>().toWellKnown()
        }
    }

/**
 * Builds [WellKnown] from Nais-injected Entra ID environment variables:
 * `AZURE_OPENID_CONFIG_ISSUER` and `AZURE_OPENID_CONFIG_JWKS_URI`.
 */
public fun getWellKnownFromEnv(): WellKnown =
    WellKnown(
        issuer = getRequiredEnvVar("AZURE_OPENID_CONFIG_ISSUER"),
        jwksUri = getRequiredEnvVar("AZURE_OPENID_CONFIG_JWKS_URI"),
    )
