package no.nav.syfo.common.journalforing.dto

/**
 * A single document within a journalpost. The [brevkode] is the
 * [no.nav.syfo.common.journalforing.Brevkode] value identifying the document type.
 */
public data class Dokument(
    val brevkode: String,
    val dokumentvarianter: List<Dokumentvariant>,
    val tittel: String? = null,
)

/**
 * Metadata about a journalført document, returned by dokarkiv in a [JournalpostResponse].
 */
public data class DokumentInfo(
    val brevkode: String? = null,
    val dokumentInfoId: Int? = null,
    val tittel: String? = null,
)

/** File type of a [Dokumentvariant] physical document. */
public enum class Filtype(
    public val value: String,
) {
    PDFA("PDFA"),
}

/** Variant format of a [Dokumentvariant]. */
public enum class Variantformat(
    public val value: String,
) {
    ARKIV("ARKIV"),
}

internal const val DOKUMENTVARIANT_FILNAVN_MAX_LENGTH: Int = 200

/**
 * A physical representation (variant) of a [Dokument], e.g. the archive PDF/A.
 *
 * @throws IllegalArgumentException if `filnavn` + `filtype` length reaches
 *  [DOKUMENTVARIANT_FILNAVN_MAX_LENGTH], which dokarkiv rejects.
 */
public data class Dokumentvariant(
    val filnavn: String,
    val filtype: String,
    val fysiskDokument: ByteArray,
    val variantformat: String,
) {
    init {
        require((filnavn.length + filtype.length) < DOKUMENTVARIANT_FILNAVN_MAX_LENGTH) {
            "Filnavn of Dokumentvariant is too long, max size is $DOKUMENTVARIANT_FILNAVN_MAX_LENGTH"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dokumentvariant

        if (filnavn != other.filnavn) return false
        if (filtype != other.filtype) return false
        if (!fysiskDokument.contentEquals(other.fysiskDokument)) return false
        if (variantformat != other.variantformat) return false

        return true
    }

    override fun hashCode(): Int {
        var result = filnavn.hashCode()
        result = 31 * result + filtype.hashCode()
        result = 31 * result + fysiskDokument.contentHashCode()
        result = 31 * result + variantformat.hashCode()
        return result
    }
}
