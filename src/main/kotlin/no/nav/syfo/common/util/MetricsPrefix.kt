package no.nav.syfo.common.util

/**
 * Prefix for all library metric names, derived from the consuming application's name.
 *
 * Read once from the NAIS-injected `NAIS_APP_NAME` environment variable (which reflects
 * `metadata.name` in the app's `app.yaml`). Dashes are replaced with underscores to
 * comply with Prometheus metric naming rules.
 *
 * Falls back to `"unknown"` if the environment variable is not set (e.g. in unit tests).
 */
internal val metricsAppPrefix: String by lazy {
    (System.getenv("NAIS_APP_NAME") ?: "unknown")
        .replace("-", "_")
}
