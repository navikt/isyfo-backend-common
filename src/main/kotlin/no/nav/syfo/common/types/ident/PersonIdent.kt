package no.nav.syfo.common.types.ident

import java.util.UUID

@JvmInline
public value class PersonIdent(
    public val value: String,
) {
    init {
        require(elevenDigits.matches(value)) {
            "Value is not a valid PersonIdent"
        }
    }

    private companion object {
        val elevenDigits = Regex("^\\d{11}\$")
    }
}

/**
 * Hashes this [PersonIdent] into a deterministic UUID string for use as a Kafka record key,
 * ensuring per-person ordering on the topic without exposing the raw identity number.
 */
public fun PersonIdent.asProducerRecordKey(): String = UUID.nameUUIDFromBytes(value.toByteArray()).toString()
