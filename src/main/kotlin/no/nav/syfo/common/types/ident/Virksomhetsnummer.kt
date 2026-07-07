package no.nav.syfo.common.types.ident

@JvmInline
public value class Virksomhetsnummer(
    public val value: String,
) {
    init {
        require(nineDigits.matches(value)) {
            "Value is not a valid Virksomhetsnummer"
        }
    }

    private companion object {
        val nineDigits = Regex("^\\d{9}\$")
    }
}
