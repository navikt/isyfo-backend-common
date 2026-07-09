package no.nav.syfo.common.distribusjon.dto

/**
 * What kind of document is being distributed. Affects e.g. which distribution channels dokdistfordeling
 * considers valid and how the distribution is prioritised.
 */
public enum class Distribusjonstype(
    public val value: String,
) {
    VEDTAK("VEDTAK"),
    VIKTIG("VIKTIG"),
    ANNET("ANNET"),
    ;

    internal companion object {
        fun fromValue(value: String): Distribusjonstype? = entries.find { it.value == value }
    }
}

/**
 * When the document may be distributed.
 *
 * - `UMIDDELBART`: distribute as soon as possible.
 * - `KJERNETID`: distribute within normal working hours ("kjernetid").
 */
public enum class Distribusjonstidspunkt(
    public val value: String,
) {
    UMIDDELBART("UMIDDELBART"),
    KJERNETID("KJERNETID"),
    ;

    internal companion object {
        fun fromValue(value: String): Distribusjonstidspunkt? = entries.find { it.value == value }
    }
}

/**
 * Overrides which channel the forsendelse is distributed through, bypassing dokdistfordeling's
 * normal channel selection (e.g. digital mailbox lookup).
 */
public enum class TvingKanal(
    public val value: String,
) {
    PRINT("PRINT"),
    TRYGDERETTEN("TRYGDERETTEN"),
    ;

    internal companion object {
        fun fromValue(value: String): TvingKanal? = entries.find { it.value == value }
    }
}

/**
 * Request body for distributing a journalpost via dokdistfordeling.
 *
 * Most consumers should build this via
 * [no.nav.syfo.common.distribusjon.createDistribuerJournalpostRequest] rather than constructing
 * it directly.
 *
 * @param journalpostId The journalpost (from dokarkiv) to distribute.
 * @param distribusjonstype See [Distribusjonstype].
 * @param distribusjonstidspunkt See [Distribusjonstidspunkt].
 * @param batchId Free-text id identifying a batch of related forsendelser, for the caller's own bookkeeping.
 * @param bestillendeFagsystem The fagsystem ordering the distribution.
 * @param adresse Overrides the postal address, see [Adresse]. Required if the recipient is a samhandler.
 * @param dokumentProdApp Application that produced the main document (for tracing/debugging).
 * @param tvingKanal Overrides the distribution channel, see [TvingKanal].
 * @param forsendelseMetadata Metadata following the forsendelse. Required together with [forsendelseMetadataType].
 * @param forsendelseMetadataType Type of [forsendelseMetadata]. Required together with [forsendelseMetadata].
 */
public data class DistribuerJournalpostRequest(
    val journalpostId: String,
    val distribusjonstype: String,
    val distribusjonstidspunkt: String,
    val batchId: String? = null,
    val bestillendeFagsystem: String? = null,
    val adresse: Adresse? = null,
    val dokumentProdApp: String? = null,
    val tvingKanal: String? = null,
    val forsendelseMetadata: String? = null,
    val forsendelseMetadataType: String? = null,
) {
    init {
        Distribusjonstype.fromValue(distribusjonstype)
            ?: throw IllegalArgumentException("Unknown distribusjonstype '$distribusjonstype'")
        Distribusjonstidspunkt.fromValue(distribusjonstidspunkt)
            ?: throw IllegalArgumentException("Unknown distribusjonstidspunkt '$distribusjonstidspunkt'")
        tvingKanal?.let {
            TvingKanal.fromValue(it) ?: throw IllegalArgumentException("Unknown tvingKanal '$it'")
        }
        require((forsendelseMetadata == null) == (forsendelseMetadataType == null)) {
            "forsendelseMetadata and forsendelseMetadataType must be set together"
        }
    }
}
