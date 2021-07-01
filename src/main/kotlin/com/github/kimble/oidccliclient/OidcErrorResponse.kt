package keystudioctl.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OidcErrorResponse(
    @SerialName("error") val error: String,
    @SerialName("error_description") val errorDescription: String?
)
