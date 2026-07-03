package no.nav.syfo.common.journalforing.dto

// See https://confluence.adeo.no/spaces/BOA/pages/313346837/opprettJournalpost for documentation

/** Default journalførende enhet — 9999 means "let dokarkiv/Norg decide". */
public const val JOURNALFORENDE_ENHET: Int = 9999

/**
 * INNGAAENDE brukes for dokumentasjon som NAV har mottatt fra en ekstern part. Dette kan være søknader, ettersendelser av dokumentasjon til sak eller meldinger fra arbeidsgivere.
 *
 * UTGAAENDE brukes for dokumentasjon som NAV har produsert og sendt ut til en ekstern part. Dette kan for eksempel være informasjons- eller vedtaksbrev til privatpersoner eller organisasjoner.
 *
 * NOTAT brukes for dokumentasjon som NAV har produsert selv og uten mål om å distribuere dette ut av NAV. Eksempler på dette er forvaltningsnotater og referater fra telefonsamtaler med brukere.
 * */
public enum class JournalpostType(
    public val value: String,
) {
    UTGAAENDE("UTGAAENDE"),
    INNGAAENDE("INNGAAENDE"),
    NOTAT("NOTAT"),
    ;

    internal companion object {
        fun fromValue(value: String): JournalpostType? = entries.find { it.value == value }
    }
}

/** Tema (fagområde) the journalpost belongs to. */
public enum class JournalpostTema(
    public val value: String,
) {
    OPPFOLGING("OPP"),
}

/**
 * Channel the document is/was distributed through.
 *
 * - `NAV_NO` (Ditt NAV): Forsendelsen er distribuert digitalt til brukers meldingsboks på nav.no.
 * - `SENTRAL_UTSKRIFT`: Forsendelsen er overført til sentral distribusjon og sendt i posten på papir.
 * - `HELSENETTET`: Forsendelsen er distribuert via Norsk Helsenett, helsesektorens løsning for elektronisk meldingsutveksling.
 */
public enum class JournalpostKanal(
    public val value: String,
) {
    DITT_NAV("NAV_NO"),
    SENTRAL_UTSKRIFT("S"),
    HELSENETTET("HELSENETTET"),
}

/**
 * Overrides the default innsyn rules so the journalpost and its documents are shown to the logged-in
 * citizen on nav.no even when the default rule set would hide them.
 *
 * - `VISES_MASKINELT_GODKJENT`: a machine process has decided the journalpost may be shown.
 * - `VISES_MANUELT_GODKJENT`: a NAV employee has reviewed and approved that it may be shown.
 */
public enum class OverstyrInnsynsregler(
    public val value: String,
) {
    VISES_MASKINELT_GODKJENT("VISES_MASKINELT_GODKJENT"),
    VISES_MANUELT_GODKJENT("VISES_MANUELT_GODKJENT"),
}

/**
 * Request body for creating a journalpost in dokarkiv.
 *
 * Most consumers should build this via
 * [no.nav.syfo.common.journalforing.createJournalpostRequest] rather than constructing it directly.
 *
 * Invariant enforced (per dokarkiv): [avsenderMottaker] must be set for [JournalpostType.INNGAAENDE]
 * (avsender) and [JournalpostType.UTGAAENDE] (mottaker), and must not be set for
 * [JournalpostType.NOTAT].
 */
public data class JournalpostRequest(
    val tittel: String,
    val dokumenter: List<Dokument>,
    val journalpostType: String,
    val eksternReferanseId: String,
    val avsenderMottaker: AvsenderMottaker? = null,
    val bruker: Bruker? = null,
    val journalfoerendeEnhet: Int? = JOURNALFORENDE_ENHET,
    val tema: String = JournalpostTema.OPPFOLGING.value,
    val kanal: String? = null,
    val sak: Sak = Sak(),
    val overstyrInnsynsregler: String? = null,
) {
    init {
        val type =
            JournalpostType.fromValue(journalpostType)
                ?: throw IllegalArgumentException("Unknown journalpostType '$journalpostType'")
        when (type) {
            JournalpostType.NOTAT -> {
                require(avsenderMottaker == null) {
                    "avsenderMottaker must not be set for journalpostType NOTAT"
                }
                require(kanal == null) {
                    "kanal must be null for NOTAT"
                }
            }

            JournalpostType.INNGAAENDE, JournalpostType.UTGAAENDE ->
                requireNotNull(avsenderMottaker) {
                    "avsenderMottaker must be set for journalpostType $type"
                }
        }
    }
}
