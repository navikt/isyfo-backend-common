package no.nav.syfo.common.token.azuread

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
                appClientId = System.getenv("AZURE_APP_CLIENT_ID"),
                appClientSecret = System.getenv("AZURE_APP_CLIENT_SECRET"),
                appWellKnownUrl = System.getenv("AZURE_APP_WELL_KNOWN_URL"),
                openidConfigTokenEndpoint = System.getenv("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
            )
    }
}
