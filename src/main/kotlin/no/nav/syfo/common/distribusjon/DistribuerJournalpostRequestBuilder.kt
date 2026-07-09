package no.nav.syfo.common.distribusjon

import no.nav.syfo.common.distribusjon.dto.Adresse
import no.nav.syfo.common.distribusjon.dto.DistribuerJournalpostRequest
import no.nav.syfo.common.distribusjon.dto.Distribusjonstidspunkt
import no.nav.syfo.common.distribusjon.dto.Distribusjonstype

/**
 * Builds a [DistribuerJournalpostRequest] for the common case, handling sensible defaults so
 * consumers don't have to assemble the low-level DTO themselves.
 *
 * The low-level DTO remains public, so apps with non-standard needs (e.g. `tvingKanal`,
 * forsendelse metadata, ...) can construct a [DistribuerJournalpostRequest] directly instead.
 *
 * @param journalpostId The journalpost (from dokarkiv) to distribute.
 * @param bestillendeFagsystem The fagsystem ordering the distribution.
 * @param distribusjonstype Defaults to [Distribusjonstype.VEDTAK].
 * @param distribusjonstidspunkt Defaults to [Distribusjonstidspunkt.UMIDDELBART].
 * @param batchId Optional free-text id identifying a batch of related forsendelser.
 * @param adresse Optionally override the postal address — see [Adresse]. Required if the recipient is a samhandler.
 */
public fun createDistribuerJournalpostRequest(
    journalpostId: String,
    bestillendeFagsystem: String,
    distribusjonstype: Distribusjonstype = Distribusjonstype.VEDTAK,
    distribusjonstidspunkt: Distribusjonstidspunkt = Distribusjonstidspunkt.UMIDDELBART,
    batchId: String? = null,
    adresse: Adresse? = null,
): DistribuerJournalpostRequest =
    DistribuerJournalpostRequest(
        journalpostId = journalpostId,
        distribusjonstype = distribusjonstype.value,
        distribusjonstidspunkt = distribusjonstidspunkt.value,
        batchId = batchId,
        bestillendeFagsystem = bestillendeFagsystem,
        adresse = adresse,
    )
