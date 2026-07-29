package no.nav.syfo.common.testutil

import java.util.Base64

fun jwtWithNavident(navident: String): String {
    val encoder = Base64.getUrlEncoder().withoutPadding()
    val header = encoder.encodeToString("""{"alg":"none","typ":"JWT"}""".toByteArray())
    val payload = encoder.encodeToString("""{"NAVident":"$navident"}""".toByteArray())
    return "$header.$payload.signature"
}
