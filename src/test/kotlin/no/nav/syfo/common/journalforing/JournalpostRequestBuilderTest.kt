package no.nav.syfo.common.journalforing

import no.nav.syfo.common.journalforing.dto.Bruker
import no.nav.syfo.common.journalforing.dto.BrukerIdType
import no.nav.syfo.common.journalforing.dto.Filtype
import no.nav.syfo.common.journalforing.dto.JOURNALFORENDE_ENHET
import no.nav.syfo.common.journalforing.dto.JournalpostKanal
import no.nav.syfo.common.journalforing.dto.JournalpostTema
import no.nav.syfo.common.journalforing.dto.JournalpostType
import no.nav.syfo.common.journalforing.dto.OverstyrInnsynsregler
import no.nav.syfo.common.journalforing.dto.Variantformat
import no.nav.syfo.common.types.ident.Personident
import no.nav.syfo.common.types.ident.Virksomhetsnummer
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class JournalpostRequestBuilderTest {
    private val bruker = Bruker("12345678910", BrukerIdType.PERSONIDENT.value)
    private val pdf = byteArrayOf(4, 5, 6)

    private object TestBrevkode : Brevkode {
        override val value = "OPPF_TEST_BREVKODE"
    }

    private fun build(mottaker: JournalpostMottaker) =
        createJournalpostRequest(
            bruker = bruker,
            mottaker = mottaker,
            brevkode = TestBrevkode,
            tittel = "Tittel",
            pdf = pdf,
            eksternReferanseId = "ref-1",
            journalpostType = JournalpostType.UTGAAENDE,
            kanal = JournalpostKanal.DITT_NAV,
        )

    @Test
    fun `maps Person mottaker to FNR avsenderMottaker`() {
        val mottakerPersonident = Personident("10987654321")
        val request = build(JournalpostMottaker.Person(mottakerPersonident, navn = "Ola"))

        assertEquals(mottakerPersonident.value, request.avsenderMottaker?.id)
        assertEquals(BrukerIdType.PERSONIDENT.value, request.avsenderMottaker?.idType)
        assertEquals("Ola", request.avsenderMottaker?.navn)
    }

    @Test
    fun `maps Virksomhet mottaker to ORGNR avsenderMottaker`() {
        val orgnr = Virksomhetsnummer("123456789")
        val request = build(JournalpostMottaker.Virksomhet(orgnr, navn = "Bedrift AS"))

        assertEquals(orgnr.value, request.avsenderMottaker?.id)
        assertEquals(BrukerIdType.VIRKSOMHETSNUMMER.value, request.avsenderMottaker?.idType)
        assertEquals("Bedrift AS", request.avsenderMottaker?.navn)
    }

    @Test
    fun `maps Behandler mottaker to HPRNR avsenderMottaker and pads HPR to nine digits`() {
        val request = build(JournalpostMottaker.Behandler(hprId = 12345, navn = "Lege Legesen"))

        assertEquals("000012345", request.avsenderMottaker?.id)
        assertEquals(BrukerIdType.HPRNR.value, request.avsenderMottaker?.idType)
        assertEquals("Lege Legesen", request.avsenderMottaker?.navn)
    }

    @Test
    fun `sets bruker to citizen FNR and applies expected defaults`() {
        val request = build(JournalpostMottaker.Person(Personident("10987654321")))

        assertEquals(bruker.id, request.bruker?.id)
        assertEquals(BrukerIdType.PERSONIDENT.value, request.bruker?.idType)
        assertEquals(JournalpostType.UTGAAENDE.value, request.journalpostType)
        assertEquals(JournalpostTema.OPPFOLGING.value, request.tema)
        assertEquals(JournalpostKanal.DITT_NAV.value, request.kanal)
        assertEquals(JOURNALFORENDE_ENHET, request.journalfoerendeEnhet)
        assertEquals("ref-1", request.eksternReferanseId)
    }

    @Test
    fun `wraps pdf in a single PDFA archive dokumentvariant named after the tittel`() {
        val request = build(JournalpostMottaker.Person(Personident("10987654321")))

        val dokument = request.dokumenter.single()
        assertEquals(TestBrevkode.value, dokument.brevkode)
        assertEquals("Tittel", dokument.tittel)

        val variant = dokument.dokumentvarianter.single()
        assertEquals("Tittel", variant.filnavn)
        assertEquals(Filtype.PDFA.value, variant.filtype)
        assertEquals(Variantformat.ARKIV.value, variant.variantformat)
        assertArrayEquals(pdf, variant.fysiskDokument)
    }

    @Test
    fun `builds a NOTAT without mottaker and leaves avsenderMottaker null`() {
        val request =
            createJournalpostRequest(
                bruker = bruker,
                brevkode = TestBrevkode,
                tittel = "Notat",
                pdf = pdf,
                eksternReferanseId = "ref-2",
                journalpostType = JournalpostType.NOTAT,
            )

        assertNull(request.avsenderMottaker)
        assertNull(request.kanal)
        assertEquals(JournalpostType.NOTAT.value, request.journalpostType)
    }

    @Test
    fun `passes overstyrInnsynsregler through when provided`() {
        val request =
            createJournalpostRequest(
                bruker = bruker,
                mottaker = JournalpostMottaker.Person(Personident("10987654321")),
                brevkode = TestBrevkode,
                tittel = "Tittel",
                pdf = pdf,
                eksternReferanseId = "ref-3",
                kanal = JournalpostKanal.DITT_NAV,
                journalpostType = JournalpostType.UTGAAENDE,
                overstyrInnsynsregler = OverstyrInnsynsregler.VISES_MASKINELT_GODKJENT,
            )

        assertEquals(OverstyrInnsynsregler.VISES_MASKINELT_GODKJENT.value, request.overstyrInnsynsregler)
    }
}
