package no.nav.syfo.common.journalforing.dto

/**
 * Identifies who a journalpost is to/from in dokarkiv.
 *
 * Ved journalposttype INNGÅENDE skal avsender av dokumentene oppgis.
 * Ved journalposttype UTGÅENDE skal mottaker av dokumentene oppgis.
 * avsenderMottaker skal ikke settes for journalposttype NOTAT.
 *
 * The [idType] values valid for [AvsenderMottaker] are defined in [BrukerIdType.avsenderMottakerTypes]:
 * `FNR`, `ORGNR`, `HPRNR`, `UTL_ORG` (utenlandsk organisasjon).
 * (`AKTOERID` is only valid for [Bruker].)
 *
 * Invariants enforced (per dokarkiv):
 * - [id] and [idType] must both be set, or both be null.
 * - When set, [idType] must be one of [BrukerIdType.avsenderMottakerTypes] and [id] must match
 *   the expected format for that type (`FNR` = 11, `ORGNR` = 9, `HPRNR` = 7-9 digits,
 *   `UTL_ORG` = non-empty string).
 *
 * [navn] is optional; for `FNR` and `ORGNR` dokarkiv resolves the name from PDL/ereg, so it does
 * not need to be supplied. When provided it should be on the format `Fornavn Mellomnavn Etternavn`.
 */
public data class AvsenderMottaker(
    val id: String?,
    val idType: String?,
    val navn: String? = null,
) {
    init {
        require((id == null) == (idType == null)) {
            "AvsenderMottaker must have both id and idType set, or neither"
        }
        if (id != null && idType != null) {
            val brukerIdType =
                BrukerIdType.fromValue(idType)
                    ?: throw IllegalArgumentException("Unknown AvsenderMottaker idType '$idType'")
            require(brukerIdType in BrukerIdType.avsenderMottakerTypes) {
                "Invalid idType '$idType' for AvsenderMottaker; valid types are ${BrukerIdType.avsenderMottakerTypes.joinToString {
                    it.value
                }}"
            }
            require(brukerIdType.isValidId(id)) {
                "AvsenderMottaker id has an invalid number of digits for brukeridType $idType"
            }
        }
    }
}
