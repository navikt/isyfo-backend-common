package no.nav.syfo.common.token

public fun interface OboTokenProvider {
    public suspend fun getOnBehalfOfToken(targetClientId: String, token: String): String?
}
