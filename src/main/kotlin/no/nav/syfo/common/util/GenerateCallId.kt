package no.nav.syfo.common.util

import java.util.UUID

public fun generateCallId(): String = UUID.randomUUID().toString()
