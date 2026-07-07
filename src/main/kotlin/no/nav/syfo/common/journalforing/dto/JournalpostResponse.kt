package no.nav.syfo.common.journalforing.dto

import no.nav.syfo.common.journalforing.JournalpostId

/**
 * Response from dokarkiv after creating a journalpost.
 */
public data class JournalpostResponse(
    val journalpostId: JournalpostId,
    val journalstatus: String,
    val dokumenter: List<DokumentInfo>? = null,
    val journalpostferdigstilt: Boolean? = null,
    val melding: String? = null,
)
