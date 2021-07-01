package com.github.kimble.oidccliclient

import fi.iki.elonen.NanoHTTPD
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.CompletableFuture
import kotlin.text.Charsets.UTF_8

internal class CallbackHttpServer(port: Int) : NanoHTTPD(port), AutoCloseable {

    val redirectUri: URI = URI.create("http://localhost:$port")
    val urlEncodedRedirectUri: String = URLEncoder.encode(redirectUri.toASCIIString(), UTF_8)
    val future = CompletableFuture<CodeCallback>()

    init {
        log.info("Staring http callback server on port {}", port)
        start()
    }

    override fun serve(session: IHTTPSession): Response {
        completeFutureWithDataFromParameters(session)
        return newFixedLengthResponse("You can close this window")
    }

    private fun completeFutureWithDataFromParameters(session: IHTTPSession) {
        val params = splitQueryStringIntoParameters(session.queryParameterString)
        val error = params["error"]
        val code = params["code"]

        if (error != null) {
            val errorDescription = params["error_description"] ?: "None"
            val ex = OidcException.ErrorResponse(error, errorDescription)
            future.completeExceptionally(ex)
        } else if (code != null) {
            val sessionState = params["session_state"]

            if (sessionState == null) {
                val ex = OidcException.InvalidCallback("Missing session_state or code")
                future.completeExceptionally(ex)
            } else {
                val data = CodeCallback(sessionState, code)
                future.complete(data)
            }
        }
    }

    /**
     * This is obviously not really correct, but good enough (tm)
     */
    private fun splitQueryStringIntoParameters(queryParameterString: String): Map<String, String> {
        return queryParameterString.split("&")
            .map { it.split("=") }
            .associate { it[0] to URLDecoder.decode(it[1], UTF_8) }
    }

    override fun close() {
        log.info("Stopping http callback server")
        stop()
    }

    companion object {
        private val log : Logger = LoggerFactory.getLogger(CallbackHttpServer::class.java)
    }
}
