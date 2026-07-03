package no.nav.syfo.common.journalforing.dto

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DokarkivDtoInvariantsTest {
    private val dokument =
        Dokument(
            brevkode = "OPPF_TEST_BREVKODE",
            dokumentvarianter =
                listOf(
                    Dokumentvariant(
                        filnavn = "fil",
                        filtype = Filtype.PDFA.value,
                        fysiskDokument = byteArrayOf(1),
                        variantformat = Variantformat.ARKIV.value,
                    ),
                ),
        )

    private val personMottaker =
        AvsenderMottaker(id = "12345678910", idType = BrukerIdType.PERSONIDENT.value)

    // AvsenderMottaker

    @Test
    fun `AvsenderMottaker requires both id and idType or neither`() {
        assertThrows(IllegalArgumentException::class.java) {
            AvsenderMottaker(id = "12345678910", idType = null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            AvsenderMottaker(id = null, idType = BrukerIdType.PERSONIDENT.value)
        }
        assertDoesNotThrow { AvsenderMottaker(id = null, idType = null, navn = "Ola Nordmann") }
    }

    @Test
    fun `AvsenderMottaker accepts valid idTypes`() {
        assertDoesNotThrow { AvsenderMottaker("12345678910", BrukerIdType.PERSONIDENT.value) }
        assertDoesNotThrow { AvsenderMottaker("123456789", BrukerIdType.VIRKSOMHETSNUMMER.value) }
        assertDoesNotThrow { AvsenderMottaker("1234567", BrukerIdType.HPRNR.value) }
        assertDoesNotThrow { AvsenderMottaker("123456789", BrukerIdType.HPRNR.value) }
        assertDoesNotThrow { AvsenderMottaker("foreign-org-123", BrukerIdType.UTL_ORG.value) }
    }

    @Test
    fun `AvsenderMottaker rejects idTypes only valid for Bruker`() {
        assertThrows(IllegalArgumentException::class.java) {
            AvsenderMottaker(id = "1234567890123", idType = BrukerIdType.AKTOERID.value)
        }
    }

    @Test
    fun `AvsenderMottaker rejects unknown idType`() {
        assertThrows(IllegalArgumentException::class.java) {
            AvsenderMottaker(id = "12345678910", idType = "UKJENT")
        }
    }

    // Bruker

    @Test
    fun `Bruker validates id digit length per idType`() {
        assertDoesNotThrow { Bruker("12345678910", BrukerIdType.PERSONIDENT.value) }
        assertDoesNotThrow { Bruker("123456789", BrukerIdType.VIRKSOMHETSNUMMER.value) }
        assertDoesNotThrow { Bruker("1234567890123", BrukerIdType.AKTOERID.value) }

        assertThrows(IllegalArgumentException::class.java) {
            Bruker("123", BrukerIdType.PERSONIDENT.value)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Bruker("12345678910", BrukerIdType.VIRKSOMHETSNUMMER.value)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Bruker("123456789", BrukerIdType.AKTOERID.value)
        }
    }

    @Test
    fun `Bruker rejects idTypes only valid for AvsenderMottaker`() {
        assertThrows(IllegalArgumentException::class.java) {
            Bruker("1234567", BrukerIdType.HPRNR.value)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Bruker("foreign-org-123", BrukerIdType.UTL_ORG.value)
        }
    }

    @Test
    fun `Bruker rejects unknown idType`() {
        assertThrows(IllegalArgumentException::class.java) {
            Bruker(id = "12345678910", idType = "UKJENT")
        }
    }

    @Test
    fun `JournalpostRequest requires avsenderMottaker for UTGAAENDE and INNGAAENDE`() {
        assertDoesNotThrow {
            request(JournalpostType.UTGAAENDE, avsenderMottaker = personMottaker)
        }
        assertDoesNotThrow {
            request(JournalpostType.INNGAAENDE, avsenderMottaker = personMottaker)
        }
        assertThrows(IllegalArgumentException::class.java) {
            request(JournalpostType.UTGAAENDE, avsenderMottaker = null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            request(JournalpostType.INNGAAENDE, avsenderMottaker = null)
        }
    }

    @Test
    fun `JournalpostRequest forbids avsenderMottaker for NOTAT`() {
        assertDoesNotThrow { request(JournalpostType.NOTAT, avsenderMottaker = null) }
        assertThrows(IllegalArgumentException::class.java) {
            request(JournalpostType.NOTAT, avsenderMottaker = personMottaker)
        }
    }

    @Test
    fun `JournalpostRequest rejects unknown journalpostType`() {
        assertThrows(IllegalArgumentException::class.java) {
            JournalpostRequest(
                tittel = "t",
                dokumenter = listOf(dokument),
                journalpostType = "UKJENT",
                eksternReferanseId = "ref",
                avsenderMottaker = personMottaker,
            )
        }
    }

    // Sak FAGSAK invariants

    @Test
    fun `Sak FAGSAK requires fagsakId and fagsaksystem`() {
        assertDoesNotThrow {
            Sak(sakstype = SaksType.FAGSAK.value, fagsakId = "1", fagsaksystem = "FS")
        }
        assertThrows(IllegalArgumentException::class.java) {
            Sak(sakstype = SaksType.FAGSAK.value, fagsakId = "1")
        }
        assertThrows(IllegalArgumentException::class.java) {
            Sak(sakstype = SaksType.FAGSAK.value)
        }
    }

    @Test
    fun `Sak GENERELL forbids fagsakId and fagsaksystem`() {
        assertDoesNotThrow { Sak() }
        assertThrows(IllegalArgumentException::class.java) {
            Sak(sakstype = SaksType.GENERELL.value, fagsakId = "1", fagsaksystem = "FS")
        }
    }

    private fun request(
        type: JournalpostType,
        avsenderMottaker: AvsenderMottaker?,
    ) = JournalpostRequest(
        tittel = "t",
        dokumenter = listOf(dokument),
        journalpostType = type.value,
        eksternReferanseId = "ref",
        avsenderMottaker = avsenderMottaker,
    )
}
