package no.nav.syfo.common.journalforing.dto

/** Type of sak a journalpost is attached to.
 *
 * `FAGSAK` means the documents belong to a sak in a fagsystem; when used, both [Sak.fagsakId] and
 * [Sak.fagsaksystem] must be supplied. `GENERELL_SAK` is for documents that do not belong to a
 * concrete fagsak — the citizen's "mappe" for a given tema.
 */
public enum class SaksType(
    public val value: String,
) {
    GENERELL("GENERELL_SAK"),
    FAGSAK("FAGSAK"),
}

/**
 * The sak a journalpost is attached to. Defaults to a generell sak ([SaksType.GENERELL]).
 *
 * Invariant enforced (per dokarkiv): when [sakstype] is `FAGSAK`, both [fagsakId] and
 * [fagsaksystem] are required; for any other sakstype they must not be set.
 */
public data class Sak(
    val sakstype: String = SaksType.GENERELL.value,
    val fagsakId: String? = null,
    val fagsaksystem: String? = null,
) {
    init {
        if (sakstype == SaksType.FAGSAK.value) {
            requireNotNull(fagsakId) { "fagsakId is required when sakstype is FAGSAK" }
            requireNotNull(fagsaksystem) { "fagsaksystem is required when sakstype is FAGSAK" }
        } else {
            require(fagsakId == null && fagsaksystem == null) {
                "fagsakId and fagsaksystem can only be set when sakstype is FAGSAK"
            }
        }
    }
}
