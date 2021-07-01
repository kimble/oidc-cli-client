package com.github.kimble.oidccliclient

import com.nimbusds.jwt.SignedJWT
import keystudioctl.auth.OidcErrorResponse
import keystudioctl.auth.OidcTokensResponse
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class OidcClient(
    private val httpClient: HttpClient,
    private val clientId: String,
    private val tokenUri: URI,
    private val logoutUri: URI
) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun logout(tokens: Tokens) {
        try {
            val encodedParams = goodEnoughFormEncode(
                mapOf(
                    "client_id" to clientId,
                    "refresh_token" to tokens.refreshToken.serialize()
                )
            )

            val request = HttpRequest.newBuilder()
                .uri(logoutUri)
                .POST(HttpRequest.BodyPublishers.ofString(encodedParams))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            checkForErrors(response)
        } catch (ex: Exception) {
            throw OidcException.LogoutFailed(ex)
        }
    }

    internal fun exchangeCodeForToken(challenge: Challenge, code: CodeCallback, redirectUri: String): Tokens {
        log.info("Exchanging code for tokens at {}", tokenUri)

        return try {
            val responseBody = postForm(
                uri = tokenUri,
                parameters = mapOf(
                    "code_verifier" to challenge.verifierCode,
                    "code" to code.code,
                    "grant_type" to "authorization_code",
                    "client_id" to clientId,
                    "redirect_uri" to redirectUri
                )
            )

            parseTokensFromString(responseBody)
        } catch (ex: Exception) {
            throw OidcException.TokenExchangeFailed(ex)
        }
    }

    private fun checkForErrors(response: HttpResponse<String>): String {
        val responseBody = response.body()

        return if (response.statusCode() > 299) {
            if (response.headers().firstValue("Content-Type").orElse("-") == "application/json") {
                try {
                    val error = json.decodeFromString(OidcErrorResponse.serializer(), responseBody)
                    throw OidcException.ErrorResponse(error.error, error.errorDescription ?: "-")
                } catch (ex: Exception) {
                    throw OidcException.Unexpected("Got json error response, but failed to parse it: $responseBody", ex)
                }
            } else {
                throw OidcException.Unexpected("Got unexpected http error response: ${response.statusCode()}: $responseBody")
            }
        } else {
            responseBody
        }
    }

    fun refresh(tokens: Tokens): Tokens {
        return try {
            val responseBody = postForm(
                uri = tokenUri,
                parameters = mapOf(
                    "grant_type" to "refresh_token",
                    "refresh_token" to tokens.refreshToken.serialize(),
                    "client_id" to clientId
                )
            )

            parseTokensFromString(responseBody)
        } catch (ex: Exception) {
            throw OidcException.TokenRefreshFailed(ex)
        }
    }

    private fun parseTokensFromString(responseBody: String): Tokens {
        val deserializer = OidcTokensResponse.serializer()
        val freshTokens = json.decodeFromString(deserializer, responseBody)

        return Tokens(
            accessToken = SignedJWT.parse(freshTokens.accessToken),
            refreshToken = SignedJWT.parse(freshTokens.refreshToken)
        )
    }

    private fun postForm(
        uri: URI,
        parameters: Map<String, String>
    ): String {
        val request = HttpRequest.newBuilder()
            .uri(uri)
            .POST(HttpRequest.BodyPublishers.ofString(goodEnoughFormEncode(parameters)))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return checkForErrors(response)
    }

    private fun goodEnoughFormEncode(params: Map<String, String>): String {
        return params.entries.joinToString(separator = "&") {
            "${it.key}=${URLEncoder.encode(it.value, Charsets.UTF_8)}"
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(OidcClient::class.java)
    }
}
