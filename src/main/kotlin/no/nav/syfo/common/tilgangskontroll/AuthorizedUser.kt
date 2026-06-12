package no.nav.syfo.common.tilgangskontroll

import no.nav.syfo.common.types.ident.Navident

/**
 * Represents the verified, authenticated Nav employee that a call was authorized for,
 * available inside the handler block of [checkPersonAndSyfoTilgang].
 *
 * [token] is always evaluated since [checkPersonAndSyfoTilgang] itself needs it.
 * [navident] is lazily evaluated and only extracted from the bearer token on first access.
 *
 * @property token The bearer token of the calling user.
 * @property navident The NAV employee identity of the calling user, lazily extracted from the
 *   bearer token. Throws [IllegalArgumentException] if accessed and the `NAVident` claim is
 *   missing from the token.
 */
public class AuthorizedUser(
    public val token: String,
    navidentProvider: () -> Navident,
) {
    public val navident: Navident by lazy(navidentProvider)
}
