package no.nav.syfo.common.journalforing.dto

/**
 * Type of identifier used for a [Bruker] or [AvsenderMottaker] in dokarkiv.
 *
 * The valid types per context are declared in [brukerTypes] and [avsenderMottakerTypes].
 *
 * Id format per type: `FNR` = 11 digits, `ORGNR` = 9 digits, `HPRNR` = 7-9 digits,
 * `AKTOERID` = 13 digits, `UTL_ORG` = non-empty string (foreign org numbers have no fixed format).
 */
public enum class BrukerIdType(
    public val value: String,
) {
    PERSONIDENT("FNR"),
    VIRKSOMHETSNUMMER("ORGNR"),
    HPRNR("HPRNR"),
    AKTOERID("AKTOERID"),
    UTL_ORG("UTL_ORG"),
    ;

    internal fun isValidId(id: String): Boolean =
        when (this) {
            PERSONIDENT -> id.matches(Regex("^\\d{11}\$"))
            VIRKSOMHETSNUMMER -> id.matches(Regex("^\\d{9}\$"))
            HPRNR -> id.matches(Regex("^\\d{7,9}\$"))
            AKTOERID -> id.matches(Regex("^\\d{13}\$"))
            UTL_ORG -> id.isNotBlank()
        }

    internal companion object {
        fun fromValue(value: String): BrukerIdType? = entries.find { it.value == value }

        /** Valid [idType] values for [Bruker]. */
        val brukerTypes: Set<BrukerIdType> = setOf(PERSONIDENT, VIRKSOMHETSNUMMER, AKTOERID)

        /** Valid [idType] values for [AvsenderMottaker]. */
        val avsenderMottakerTypes: Set<BrukerIdType> = setOf(PERSONIDENT, VIRKSOMHETSNUMMER, HPRNR, UTL_ORG)
    }
}

/**
 * The user (bruker) a journalpost concerns.
 *
 * Valid [idType] values: `FNR` (11 digits), `ORGNR` (9 digits), `AKTOERID` (13 digits).
 * `HPRNR` and `UTL_ORG` are not valid for [Bruker]; see [AvsenderMottaker] for those.
 *
 * Invariants enforced:
 * - [idType] must be one of [BrukerIdType.brukerTypes].
 * - [id] must match the expected format for the given [idType].
 */
public data class Bruker(
    val id: String,
    val idType: String,
) {
    init {
        val brukerIdType =
            BrukerIdType.fromValue(idType)
                ?: throw IllegalArgumentException("Unknown Bruker idType '$idType'")
        require(brukerIdType in BrukerIdType.brukerTypes) {
            "Invalid idType '$idType' for Bruker; valid types are ${BrukerIdType.brukerTypes.joinToString { it.value }}"
        }
        require(brukerIdType.isValidId(id)) {
            "Bruker id has an invalid format for idType $idType"
        }
    }
}
