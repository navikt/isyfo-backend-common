package no.nav.syfo.common.tilgangskontroll

import no.nav.syfo.common.types.ident.NavIdent
import no.nav.syfo.common.types.ident.PersonIdent

/**
 * Represents a verified, authorized call context, available inside the handler block of
 * [checkPersonAndSyfoTilgang].
 *
 * [personIdent], [token] and [callId] are always evaluated since [checkPersonAndSyfoTilgang]
 * itself needs them. [navIdent] is lazily evaluated and only extracted from the bearer token
 * on first access.
 *
 * @property personIdent The person the call was authorized for.
 * @property token The bearer token of the calling user.
 * @property callId The call id used for distributed tracing.
 * @property navIdent The NAV employee identity of the calling user, lazily extracted from the
 *   bearer token. Throws [IllegalArgumentException] if accessed and the `NAVident` claim is
 *   missing from the token.
 */
public class AuthorizedCall(
    public val personIdent: PersonIdent,
    public val token: String,
    public val callId: String,
    navIdentProvider: () -> NavIdent,
) {
    public val navIdent: NavIdent by lazy(navIdentProvider)
}
