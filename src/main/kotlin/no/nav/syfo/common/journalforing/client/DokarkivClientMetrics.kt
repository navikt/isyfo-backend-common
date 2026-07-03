package no.nav.syfo.common.journalforing.client

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.syfo.common.util.metricsAppPrefix

private const val CALL_DOKARKIV_JOURNALPOST_BASE = "call_dokarkiv_journalpost"

internal val COUNT_CALL_DOKARKIV_JOURNALPOST_SUCCESS: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_DOKARKIV_JOURNALPOST_BASE}_success_count")
        .description("Counts the number of successful calls to dokarkiv - journalpost")
        .register(Metrics.globalRegistry)
}

internal val COUNT_CALL_DOKARKIV_JOURNALPOST_CONFLICT: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_DOKARKIV_JOURNALPOST_BASE}_conflict_count")
        .description("Counts the number of calls to dokarkiv - journalpost resulting in 409 Conflict")
        .register(Metrics.globalRegistry)
}

internal val COUNT_CALL_DOKARKIV_JOURNALPOST_FAIL: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_DOKARKIV_JOURNALPOST_BASE}_fail_count")
        .description("Counts the number of failed calls to dokarkiv - journalpost")
        .register(Metrics.globalRegistry)
}
