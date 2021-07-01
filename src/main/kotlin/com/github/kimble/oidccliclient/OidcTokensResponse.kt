package keystudioctl.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OidcTokensResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)
