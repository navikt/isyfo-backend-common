package no.nav.syfo.common.token

public fun interface OboTokenProvider {
    public suspend fun getOnBehalfOfToken(scopeClientId: String, token: String): String?
}
