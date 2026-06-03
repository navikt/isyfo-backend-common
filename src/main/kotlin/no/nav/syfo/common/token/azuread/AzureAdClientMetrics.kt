package no.nav.syfo.common.token.azuread

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.syfo.common.util.metricsAppPrefix

private const val CALL_AZUREAD_SYSTEM_TOKEN_CACHE_BASE = "call_azuread_system_token_cache"

internal val COUNT_CALL_AZUREAD_SYSTEM_TOKEN_CACHE_HIT: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_AZUREAD_SYSTEM_TOKEN_CACHE_BASE}_hit_count")
        .description("Counts the number of cache hits for calls to Azure AD for a system token")
        .register(Metrics.globalRegistry)
}

internal val COUNT_CALL_AZUREAD_SYSTEM_TOKEN_CACHE_MISS: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_AZUREAD_SYSTEM_TOKEN_CACHE_BASE}_miss_count")
        .description("Counts the number of cache misses for calls to Azure AD for a system token")
        .register(Metrics.globalRegistry)
}
