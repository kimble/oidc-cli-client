package com.github.kimble.oidccliclient

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES
import java.util.Date

internal class TokensTest {

    @Test
    fun `Calculate access token expire time`() {
        val rightNow = Instant.now().truncatedTo(MINUTES)
        val expire = rightNow.plus(5, MINUTES)

        val signedToken = createSignedToken {
            expirationTime(Date.from(expire))
        }

        val tokens = Tokens(signedToken, signedToken)
        val duration = tokens.calculateAccessTokenTimeUntilExpire(rightNow)
        val expectedDuration = Duration.ofMinutes(5)

        assertEquals(expectedDuration, duration)
    }

    @Test
    fun `Calculate access token expire time of expired token`() {
        val rightNow = Instant.now().truncatedTo(MINUTES)
        val expire = rightNow.plus(5, MINUTES)

        val signedToken = createSignedToken {
            expirationTime(Date.from(expire))
        }

        val tokens = Tokens(signedToken, signedToken)
        val duration = tokens.calculateAccessTokenTimeUntilExpire(rightNow.plus(6, MINUTES))
        val expectedDuration = Duration.ofMinutes(-1)

        assertEquals(expectedDuration, duration)
    }

    private fun createSignedToken(claimsBuilderBlock: JWTClaimsSet.Builder.() -> Unit): SignedJWT {
        val senderJWK = RSAKeyGenerator(2048)
            .keyID("123")
            .keyUse(KeyUse.SIGNATURE)
            .generate()

        val claimsBuilder = JWTClaimsSet.Builder()
        claimsBuilderBlock(claimsBuilder)

        val signedJWT = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.RS256).keyID(senderJWK.keyID).build(),
            claimsBuilder.build()
        )

        signedJWT.sign(RSASSASigner(senderJWK))
        return signedJWT
    }
}
