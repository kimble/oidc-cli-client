package com.github.kimble.oidccliclient

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit.MINUTES

object AuthProcess {

    private val log: Logger = LoggerFactory.getLogger(AuthProcess::class.java)

    fun start(
        oidcClient: OidcClient,
        callbackPort: Int,
        configuration: OidcConfiguration
    ): Tokens {
        return try {
            val challenge = Challenge.generateRandom()

            CallbackHttpServer(callbackPort).use { callbackServer ->
                val browserAuthUri = configuration.authUri.toString() +
                    "?redirect_uri=${callbackServer.urlEncodedRedirectUri}" +
                    "&client_id=${configuration.clientId}" +
                    "&response_type=code" +
                    "&code_challenge=${challenge.sha256}" +
                    "&code_challenge_method=S256"

                log.info("Will use callback uri: $browserAuthUri")
                log.info("Please open the following uri in your browser if it doesnt happen automagically..")
                ProcessBuilder("sensible-browser", browserAuthUri).start()

                log.info("Waiting for callback...")
                val result = callbackServer.future.get(3, MINUTES)

                log.info("Got code from Keycloak, will attempt to exchange it for tokens...")
                oidcClient.exchangeCodeForToken(challenge, result, callbackServer.redirectUri.toASCIIString())
            }
        } catch (ex: Exception) {
            throw OidcException.AuthFailed(ex)
        }
    }
}
