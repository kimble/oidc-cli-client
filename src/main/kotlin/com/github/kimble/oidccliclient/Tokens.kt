package com.github.kimble.oidccliclient

import com.nimbusds.jwt.SignedJWT

class Tokens(
    val accessToken: SignedJWT,
    val refreshToken: SignedJWT
)
