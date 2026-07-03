package no.nav.syfo.common.journalforing

/**
 * A dokarkiv "brevkode" — the code identifying the type of document being journalført.
 *
 * Brevkoder are specific to each consuming application, so the library does not ship a fixed
 * set. Instead, each app implements this interface (typically as an enum) and supplies its own
 * brevkoder when building a journalpost. Only [value] (the string sent to dokarkiv) is needed
 * by the library.
 *
 * Example:
 * ```
 * enum class MyBrevkode(override val value: String) : Brevkode {
 *     FORHANDSVARSEL("OPPF_MIN_APP_FORHANDSVARSEL"),
 *     VEDTAK("OPPF_MIN_APP_VEDTAK"),
 * }
 * ```
 */
public interface Brevkode {
    public val value: String
}
