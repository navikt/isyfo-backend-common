package no.nav.syfo.common.types.ident

import java.util.UUID

@JvmInline
public value class Personident(
    public val value: String,
) {
    init {
        require(elevenDigits.matches(value)) {
            "Value is not a valid Personident"
        }
    }

    private companion object {
        val elevenDigits = Regex("^\\d{11}\$")
    }
}

/**
 * Hashes this [Personident] into a deterministic UUID string for use as a Kafka record key,
 * ensuring per-person ordering on the topic without exposing the raw identity number.
 */
public fun Personident.asProducerRecordKey(): String = UUID.nameUUIDFromBytes(value.toByteArray()).toString()
