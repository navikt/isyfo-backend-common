package no.nav.syfo.common.mock.journalforing

import no.nav.syfo.common.journalforing.JournalpostId
import no.nav.syfo.common.journalforing.dto.JournalpostResponse

/** Default journalpostId returned by [mockDokarkivRequestHandler]. */
public const val MOCK_JOURNALPOST_ID: String = "1"

/**
 * Builds a default successful [JournalpostResponse] for use in test mock handlers.
 */
public fun mockJournalpostResponse(
    journalpostId: JournalpostId = JournalpostId(MOCK_JOURNALPOST_ID),
    journalpostferdigstilt: Boolean = true,
): JournalpostResponse =
    JournalpostResponse(
        journalpostId = journalpostId,
        journalstatus = "FERDIGSTILT",
        journalpostferdigstilt = journalpostferdigstilt,
        dokumenter = emptyList(),
    )
