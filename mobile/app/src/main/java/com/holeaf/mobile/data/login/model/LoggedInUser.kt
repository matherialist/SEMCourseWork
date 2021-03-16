package com.holeaf.mobile.data.login.model

import com.holeaf.mobile.Role

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
        val userId: String,
        val id: Int,
        val displayName: String,
        val permissions: Role
)