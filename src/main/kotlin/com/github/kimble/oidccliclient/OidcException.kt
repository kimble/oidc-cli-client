package com.github.kimble.oidccliclient

sealed class OidcException(message: String, cause: Exception? = null) : IllegalStateException(message, cause) {

    class ErrorResponse(val error: String, val errorDescription: String) :
        OidcException("Error response, code: $error, description: $errorDescription")

    class InvalidCallback(message: String, cause: Exception? = null) : OidcException(message, cause)

    class TokenRefreshFailed(cause: Exception) : OidcException("Token refresh failed", cause)

    class Unexpected(message: String, cause: Exception? = null) : OidcException(message, cause)

    class LogoutFailed(cause: Exception) : OidcException("Logout failed", cause)

    class TokenExchangeFailed(cause: Exception) : OidcException("Code for token exchange failed", cause)

    class AuthFailed(cause: Exception) : OidcException("Authentication failed", cause)
}
