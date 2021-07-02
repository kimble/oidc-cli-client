package com.github.kimble.oidccliclient

import java.net.URI

class OidcConfiguration(
    val clientId: String,
    val tokenUri: URI,
    val logoutUri: URI,
    val authUri: URI
) {

    companion object {
        fun keycloak(baseUri: URI, realm: String, clientId: String): OidcConfiguration {
            val normalizedBaseUri = baseUri.toString().removeSuffix("/")

            return OidcConfiguration(
                clientId = clientId,
                authUri = URI.create("$normalizedBaseUri/auth/realms/$realm/protocol/openid-connect/auth"),
                logoutUri = URI.create("$normalizedBaseUri/auth/realms/$realm/protocol/openid-connect/logout"),
                tokenUri = URI.create("$normalizedBaseUri/auth/realms/$realm/protocol/openid-connect/token")
            )
        }
    }
}
