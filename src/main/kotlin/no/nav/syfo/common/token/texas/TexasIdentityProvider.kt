package no.nav.syfo.common.token.texas

/**
 * Identity providers supported by the Nais token exchange sidecar (Texas).
 *
 * Currently only [ENTRA_ID] is implemented. Additional providers supported by Texas —
 * such as TokenX (for citizen-facing apps via ID-porten) and Maskinporten (for
 * machine-to-machine communication between organizations) — can be added here along
 * with corresponding client classes implementing [no.nav.syfo.common.token.OboTokenProvider]
 * or [no.nav.syfo.common.token.SystemTokenProvider] as appropriate.
 *
 * @see <a href="https://docs.nais.io/auth/">Nais auth documentation</a>
 */
public enum class TexasIdentityProvider(internal val value: String) {
    /** Entra ID (formerly Azure AD) — for employees and internal services. */
    ENTRA_ID("entra_id")
}

internal fun TexasIdentityProvider.formatTarget(targetClientId: String): String =
    when (this) {
        TexasIdentityProvider.ENTRA_ID -> "api://$targetClientId/.default"
    }
