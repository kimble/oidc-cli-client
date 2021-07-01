package com.github.kimble.oidccliclient

import java.security.MessageDigest
import java.util.Base64
import java.util.UUID
import kotlin.text.Charsets.US_ASCII

/**
 * The general idea is to generate a random challenge and send a checksum of
 * this challenge together with the initial authentication request. Instead of getting
 * the tokens back with the http redirect we get a code and only by sending the
 * original challenge together with the code we get the tokens.
 */
internal data class Challenge(val verifierCode: String) {

    val sha256: String by lazy {
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        val checksum = digest.digest(verifierCode.toByteArray(US_ASCII))
        val encoder = Base64.getUrlEncoder().withoutPadding()

        encoder.encodeToString(checksum)
    }

    val method = "S256"

    companion object {
        fun generateRandom(): Challenge {
            val code = (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replace(Regex("[^a-zA-Z0-9]"), "")
            return Challenge(code)
        }
    }
}
