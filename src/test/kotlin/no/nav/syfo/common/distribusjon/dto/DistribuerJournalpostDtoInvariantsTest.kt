package no.nav.syfo.common.distribusjon.dto

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DistribuerJournalpostDtoInvariantsTest {
    private fun request(
        tvingKanal: String? = null,
        forsendelseMetadata: String? = null,
        forsendelseMetadataType: String? = null,
    ) = DistribuerJournalpostRequest(
        journalpostId = "123",
        distribusjonstype = Distribusjonstype.VEDTAK.value,
        distribusjonstidspunkt = Distribusjonstidspunkt.UMIDDELBART.value,
        tvingKanal = tvingKanal,
        forsendelseMetadata = forsendelseMetadata,
        forsendelseMetadataType = forsendelseMetadataType,
    )

    // DistribuerJournalpostRequest

    @Test
    fun `DistribuerJournalpostRequest accepts valid distribusjonstype and distribusjonstidspunkt`() {
        assertDoesNotThrow { request() }
    }

    @Test
    fun `DistribuerJournalpostRequest rejects unknown distribusjonstype`() {
        assertThrows(IllegalArgumentException::class.java) {
            DistribuerJournalpostRequest(
                journalpostId = "123",
                distribusjonstype = "UKJENT",
                distribusjonstidspunkt = Distribusjonstidspunkt.UMIDDELBART.value,
            )
        }
    }

    @Test
    fun `DistribuerJournalpostRequest rejects unknown distribusjonstidspunkt`() {
        assertThrows(IllegalArgumentException::class.java) {
            DistribuerJournalpostRequest(
                journalpostId = "123",
                distribusjonstype = Distribusjonstype.VEDTAK.value,
                distribusjonstidspunkt = "UKJENT",
            )
        }
    }

    @Test
    fun `DistribuerJournalpostRequest accepts valid tvingKanal and rejects unknown tvingKanal`() {
        assertDoesNotThrow { request(tvingKanal = TvingKanal.PRINT.value) }
        assertThrows(IllegalArgumentException::class.java) {
            request(tvingKanal = "UKJENT")
        }
    }

    @Test
    fun `DistribuerJournalpostRequest requires forsendelseMetadata and forsendelseMetadataType together`() {
        assertDoesNotThrow { request() }
        assertDoesNotThrow {
            request(forsendelseMetadata = "meta", forsendelseMetadataType = "DPO_AVTALEMELDING")
        }
        assertThrows(IllegalArgumentException::class.java) {
            request(forsendelseMetadata = "meta", forsendelseMetadataType = null)
        }
        assertThrows(IllegalArgumentException::class.java) {
            request(forsendelseMetadata = null, forsendelseMetadataType = "DPO_AVTALEMELDING")
        }
    }

    // Adresse

    @Test
    fun `Adresse norskPostadresse requires postnummer and poststed`() {
        assertDoesNotThrow {
            Adresse(adressetype = Adressetype.NORSK_POSTADRESSE.value, postnummer = "0505", poststed = "Oslo")
        }
        assertThrows(IllegalArgumentException::class.java) {
            Adresse(adressetype = Adressetype.NORSK_POSTADRESSE.value, postnummer = "0505")
        }
        assertThrows(IllegalArgumentException::class.java) {
            Adresse(adressetype = Adressetype.NORSK_POSTADRESSE.value, poststed = "Oslo")
        }
    }

    @Test
    fun `Adresse utenlandskPostadresse requires adresselinje1 and forbids postnummer and poststed`() {
        assertDoesNotThrow {
            Adresse(adressetype = Adressetype.UTENLANDSK_POSTADRESSE.value, adresselinje1 = "Eksempelveien 11B")
        }
        assertThrows(IllegalArgumentException::class.java) {
            Adresse(adressetype = Adressetype.UTENLANDSK_POSTADRESSE.value)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Adresse(
                adressetype = Adressetype.UTENLANDSK_POSTADRESSE.value,
                adresselinje1 = "Eksempelveien 11B",
                postnummer = "0505",
            )
        }
    }

    @Test
    fun `Adresse rejects unknown adressetype`() {
        assertThrows(IllegalArgumentException::class.java) {
            Adresse(adressetype = "UKJENT")
        }
    }
}
