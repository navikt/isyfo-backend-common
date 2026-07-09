package no.nav.syfo.common.distribusjon.dto

/**
 * Type of postal address in an [Adresse]. Determines which fields are required.
 */
public enum class Adressetype(
    public val value: String,
) {
    NORSK_POSTADRESSE("norskPostadresse"),
    UTENLANDSK_POSTADRESSE("utenlandskPostadresse"),
    ;

    internal companion object {
        fun fromValue(value: String): Adressetype? = entries.find { it.value == value }
    }
}

/**
 * Postal address to distribute a journalpost to, used to override the address dokdistfordeling
 * would otherwise look up from the population register. Required when the recipient is a
 * samhandler; otherwise dokdistfordeling looks up the address itself if this is not set.
 *
 * Invariant enforced (per dokdistfordeling): when [adressetype] is `norskPostadresse`,
 * [postnummer] and [poststed] are required; when `utenlandskPostadresse`, [adresselinje1] is
 * required and [postnummer]/[poststed] must be left empty (any postal code/place goes into the
 * address lines instead).
 */
public data class Adresse(
    val adressetype: String,
    val postnummer: String? = null,
    val poststed: String? = null,
    val adresselinje1: String? = null,
    val adresselinje2: String? = null,
    val adresselinje3: String? = null,
    val land: String? = null,
) {
    init {
        val type =
            Adressetype.fromValue(adressetype)
                ?: throw IllegalArgumentException("Unknown adressetype '$adressetype'")
        when (type) {
            Adressetype.NORSK_POSTADRESSE -> {
                requireNotNull(postnummer) { "postnummer is required when adressetype is norskPostadresse" }
                requireNotNull(poststed) { "poststed is required when adressetype is norskPostadresse" }
            }

            Adressetype.UTENLANDSK_POSTADRESSE -> {
                requireNotNull(adresselinje1) { "adresselinje1 is required when adressetype is utenlandskPostadresse" }
                require(postnummer == null && poststed == null) {
                    "postnummer and poststed must not be set when adressetype is utenlandskPostadresse"
                }
            }
        }
    }
}
