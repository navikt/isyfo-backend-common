package no.nav.syfo.common.token.azuread

import no.nav.syfo.common.util.getRequiredEnvVar

public data class AzureAdClientConfig(
    val appClientId: String,
    val appClientSecret: String,
    val appWellKnownUrl: String,
    val openidConfigTokenEndpoint: String,
) {
    public companion object {
        /**
         * Creates an [AzureAdClientConfig] from NAIS-injected environment variables:
         * `AZURE_APP_CLIENT_ID`, `AZURE_APP_CLIENT_SECRET`, `AZURE_APP_WELL_KNOWN_URL`,
         * `AZURE_OPENID_CONFIG_TOKEN_ENDPOINT`.
         */
        public fun fromEnv(): AzureAdClientConfig =
            AzureAdClientConfig(
                appClientId = getRequiredEnvVar("AZURE_APP_CLIENT_ID"),
                appClientSecret = getRequiredEnvVar("AZURE_APP_CLIENT_SECRET"),
                appWellKnownUrl = getRequiredEnvVar("AZURE_APP_WELL_KNOWN_URL"),
                openidConfigTokenEndpoint = getRequiredEnvVar("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
            )
    }
}
