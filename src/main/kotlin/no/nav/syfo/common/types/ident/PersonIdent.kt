package no.nav.syfo.common.types.ident

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
