package no.nav.syfo.common.util

/**
 * Reads a required environment variable and fails fast with the missing variable name.
 */
public fun getRequiredEnvVar(name: String): String = System.getenv(name) ?: error("Missing required environment variable $name")
