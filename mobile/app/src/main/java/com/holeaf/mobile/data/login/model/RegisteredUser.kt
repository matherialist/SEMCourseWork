package com.holeaf.mobile.data.login.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class RegisteredUser(
        val userId: String,
        val password: String
)