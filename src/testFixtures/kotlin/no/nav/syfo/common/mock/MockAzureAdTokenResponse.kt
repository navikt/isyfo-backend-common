package no.nav.syfo.common.mock

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Mock response for the Azure AD token endpoint.
 * Use in test HTTP mock handlers to simulate a successful token response.
 *
 * Serializes to `{"access_token":"...","expires_in":...,"token_type":"..."}`.
 */
public data class MockAzureAdTokenResponse(
    @field:JsonProperty("access_token") val accessToken: String = "mock-access-token",
    @field:JsonProperty("expires_in") val expiresIn: Long = 3600,
    @field:JsonProperty("token_type") val tokenType: String = "Bearer",
)
