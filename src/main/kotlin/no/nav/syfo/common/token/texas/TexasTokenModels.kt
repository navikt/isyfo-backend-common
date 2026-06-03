package no.nav.syfo.common.token.texas

internal data class TexasTokenExchangeRequest(
    val identity_provider: String,
    val target: String,
    val user_token: String,
)

internal data class TexasTokenRequest(
    val identity_provider: String,
    val target: String,
)

internal data class TexasTokenResponse(
    val access_token: String,
    val expires_in: Int,
    val token_type: String,
)
