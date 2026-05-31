package no.nav.syfo.common.person

/**
 * Value type representing a Norwegian national identity number (fødselsnummer or D-nummer).
 *
 * Validates that the value is exactly 11 digits. Throws [IllegalArgumentException] on invalid input.
 */
public data class PersonIdent(val value: String) {
    init {
        if (!PATTERN.matches(value)) {
            throw IllegalArgumentException("Value is not a valid PersonIdent")
        }
    }

    private companion object {
        val PATTERN = Regex("^\\d{11}$")
    }
}
