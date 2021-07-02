package com.github.kimble.oidccliclient

import com.nimbusds.jwt.SignedJWT
import java.time.Duration
import java.time.Instant

class Tokens(
    val accessToken: SignedJWT,
    val refreshToken: SignedJWT
) {

    fun calculateAccessTokenTimeUntilExpire(rightNow: Instant): Duration {
        return Duration.between(rightNow, accessToken.jwtClaimsSet.expirationTime.toInstant())
    }
}
