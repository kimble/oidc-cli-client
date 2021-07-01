package com.github.kimble.oidccliclient

internal data class CodeCallback(
    val sessionState: String,
    val code: String
)
