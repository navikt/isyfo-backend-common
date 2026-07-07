package no.nav.syfo.common.journalforing

import no.nav.syfo.common.types.ident.Personident
import no.nav.syfo.common.types.ident.Virksomhetsnummer

/**
 * The recipient (avsender/mottaker) of a journalpost.
 *
 * Models the three kinds of recipients supported by dokarkiv journalføring in iSyfo:
 * a citizen ([Person]), an employer/organisation ([Virksomhet]), or a healthcare provider
 * ([Behandler], identified by an HPR number).
 */
public sealed interface JournalpostMottaker {
    /** The optional display name of the recipient, forwarded to dokarkiv as `navn`. */
    public val navn: String?

    /** A citizen recipient, identified by a [Personident] (FNR/DNR). */
    public data class Person(
        val personident: Personident,
        override val navn: String? = null,
    ) : JournalpostMottaker

    /** An employer/organisation recipient, identified by a [Virksomhetsnummer] (ORGNR). */
    public data class Virksomhet(
        val virksomhetsnummer: Virksomhetsnummer,
        override val navn: String? = null,
    ) : JournalpostMottaker

    /** A healthcare provider recipient, identified by an HPR number. */
    public data class Behandler(
        val hprId: Int,
        override val navn: String? = null,
    ) : JournalpostMottaker
}
