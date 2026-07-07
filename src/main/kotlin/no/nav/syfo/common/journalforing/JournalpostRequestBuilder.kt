package no.nav.syfo.common.journalforing

import no.nav.syfo.common.journalforing.dto.AvsenderMottaker
import no.nav.syfo.common.journalforing.dto.Bruker
import no.nav.syfo.common.journalforing.dto.BrukerIdType
import no.nav.syfo.common.journalforing.dto.Dokument
import no.nav.syfo.common.journalforing.dto.Dokumentvariant
import no.nav.syfo.common.journalforing.dto.Filtype
import no.nav.syfo.common.journalforing.dto.JOURNALFORENDE_ENHET
import no.nav.syfo.common.journalforing.dto.JournalpostKanal
import no.nav.syfo.common.journalforing.dto.JournalpostRequest
import no.nav.syfo.common.journalforing.dto.JournalpostTema
import no.nav.syfo.common.journalforing.dto.JournalpostType
import no.nav.syfo.common.journalforing.dto.OverstyrInnsynsregler
import no.nav.syfo.common.journalforing.dto.Variantformat

/**
 * Builds a [JournalpostRequest] for a single PDF/A document, handling the common iSyfo
 * journalføring conventions so consumers don't have to assemble the low-level DTOs themselves:
 *
 * - maps the [mottaker] to the correct [AvsenderMottaker] id/idType (FNR/ORGNR/HPRNR),
 * - left-pads behandler HPR numbers to 9 digits,
 * - wraps the [pdf] in a single archive ([Variantformat.ARKIV]) PDF/A ([Filtype.PDFA]) document.
 *
 * The low-level DTOs remain public, so apps with non-standard needs (e.g. several documents, a
 * `FAGSAK`, ...) can construct a [JournalpostRequest] directly instead.
 *
 * Per dokarkiv, [mottaker] must be set for [JournalpostType.INNGAAENDE] (avsender) and
 * [JournalpostType.UTGAAENDE] (mottaker), and must be `null` for [JournalpostType.NOTAT]; this is
 * validated by [JournalpostRequest].
 *
 * @param bruker The person or organisation the journalpost concerns — a [Bruker] with [idType]
 *  `FNR` (11 digits), `ORGNR` (9 digits), or `AKTOERID` (13 digits).
 * @param brevkode The app-specific [Brevkode] identifying the document type.
 * @param tittel Title of the journalpost and document, also used as the file name.
 * @param pdf The document as a PDF/A byte array.
 * @param eksternReferanseId Reference to ID from consumer, eg. uuid for vedtak, vurdering etc; dokarkiv returns 409 Conflict on reuse.
 * @param mottaker The recipient of the journalpost — see [JournalpostMottaker]. `null` for NOTAT.
 * @param journalpostType Defaults to [JournalpostType.UTGAAENDE].
 * @param kanal The distribution [JournalpostKanal], if any.
 * @param tema Defaults to [JournalpostTema.OPPFOLGING].
 * @param journalfoerendeEnhet Defaults to [JOURNALFORENDE_ENHET] (9999).
 * @param overstyrInnsynsregler Optionally override the default innsyn rules — see [OverstyrInnsynsregler].
 */
public fun createJournalpostRequest(
    bruker: Bruker,
    brevkode: Brevkode,
    tittel: String,
    pdf: ByteArray,
    eksternReferanseId: String,
    journalpostType: JournalpostType,
    mottaker: JournalpostMottaker? = null,
    kanal: JournalpostKanal? = null,
    tema: JournalpostTema = JournalpostTema.OPPFOLGING,
    journalfoerendeEnhet: Int? = JOURNALFORENDE_ENHET,
    overstyrInnsynsregler: OverstyrInnsynsregler? = null,
): JournalpostRequest {
    val dokumenter =
        listOf(
            Dokument(
                brevkode = brevkode.value,
                tittel = tittel,
                dokumentvarianter =
                    listOf(
                        Dokumentvariant(
                            filnavn = tittel,
                            filtype = Filtype.PDFA.value,
                            fysiskDokument = pdf,
                            variantformat = Variantformat.ARKIV.value,
                        ),
                    ),
            ),
        )

    return JournalpostRequest(
        avsenderMottaker = mottaker?.toAvsenderMottaker(),
        tittel = tittel,
        bruker = bruker,
        dokumenter = dokumenter,
        journalpostType = journalpostType.value,
        eksternReferanseId = eksternReferanseId,
        journalfoerendeEnhet = journalfoerendeEnhet,
        tema = tema.value,
        kanal = kanal?.value,
        overstyrInnsynsregler = overstyrInnsynsregler?.value,
    )
}

private fun JournalpostMottaker.toAvsenderMottaker(): AvsenderMottaker =
    when (this) {
        is JournalpostMottaker.Person ->
            AvsenderMottaker(
                id = personident.value,
                idType = BrukerIdType.PERSONIDENT.value,
                navn = navn,
            )

        is JournalpostMottaker.Virksomhet ->
            AvsenderMottaker(
                id = virksomhetsnummer.value,
                idType = BrukerIdType.VIRKSOMHETSNUMMER.value,
                navn = navn,
            )

        is JournalpostMottaker.Behandler ->
            AvsenderMottaker(
                id = hprNrWithNineDigits(hprId),
                idType = BrukerIdType.HPRNR.value,
                navn = navn,
            )
    }

private fun hprNrWithNineDigits(hprnummer: Int): String = hprnummer.toString().padStart(9, '0')
