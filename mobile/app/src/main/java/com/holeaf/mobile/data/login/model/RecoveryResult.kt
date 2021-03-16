package com.holeaf.mobile.data.login.model

/**
 * Authentication result : success (user details) or error message.
 */
data class RecoveryResult(
    val success: Boolean? = null,
    val error: Int? = null
)