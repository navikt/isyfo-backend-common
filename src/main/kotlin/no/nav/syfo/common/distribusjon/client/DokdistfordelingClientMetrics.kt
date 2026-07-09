package no.nav.syfo.common.distribusjon.client

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.syfo.common.util.metricsAppPrefix

private const val CALL_DOKDISTFORDELING_DISTRIBUER_BASE = "call_dokdistfordeling_distribuer"

internal val COUNT_CALL_DOKDISTFORDELING_DISTRIBUER_SUCCESS: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_DOKDISTFORDELING_DISTRIBUER_BASE}_success_count")
        .description("Counts the number of successful calls to dokdistfordeling - distribuerjournalpost")
        .register(Metrics.globalRegistry)
}

internal val COUNT_CALL_DOKDISTFORDELING_DISTRIBUER_CONFLICT: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_DOKDISTFORDELING_DISTRIBUER_BASE}_conflict_count")
        .description("Counts the number of calls to dokdistfordeling - distribuerjournalpost resulting in 409 Conflict")
        .register(Metrics.globalRegistry)
}

internal val COUNT_CALL_DOKDISTFORDELING_DISTRIBUER_FAIL: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_DOKDISTFORDELING_DISTRIBUER_BASE}_fail_count")
        .description("Counts the number of failed calls to dokdistfordeling - distribuerjournalpost")
        .register(Metrics.globalRegistry)
}
