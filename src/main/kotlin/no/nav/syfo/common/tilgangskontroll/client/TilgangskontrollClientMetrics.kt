package no.nav.syfo.common.tilgangskontroll.client

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.syfo.common.util.metricsAppPrefix

private const val CALL_TILGANGSKONTROLL_PERSON_BASE = "call_tilgangskontroll_person"

internal val COUNT_CALL_TILGANGSKONTROLL_PERSON_SUCCESS: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_TILGANGSKONTROLL_PERSON_BASE}_success_count")
        .description("Counts the number of successful calls to istilgangskontroll - person")
        .register(Metrics.globalRegistry)
}

internal val COUNT_CALL_TILGANGSKONTROLL_PERSON_FAIL: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_TILGANGSKONTROLL_PERSON_BASE}_fail_count")
        .description("Counts the number of failed calls to istilgangskontroll - person")
        .register(Metrics.globalRegistry)
}

internal val COUNT_CALL_TILGANGSKONTROLL_PERSON_FORBIDDEN: Counter by lazy {
    Counter
        .builder("${metricsAppPrefix}_${CALL_TILGANGSKONTROLL_PERSON_BASE}_forbidden_count")
        .description("Counts the number of forbidden calls to istilgangskontroll - person")
        .register(Metrics.globalRegistry)
}
