package no.nav.syfo.common.token

public interface SystemTokenProvider {
    public suspend fun getSystemToken(targetClientId: String): String?
}
