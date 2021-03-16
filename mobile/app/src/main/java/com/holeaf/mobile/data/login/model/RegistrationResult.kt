package com.holeaf.mobile.data.login.model

import com.holeaf.mobile.ui.login.LoggedInUserView

/**
 * Authentication result : success (user details) or error message.
 */
data class RegistrationResult(
    val success: LoggedInUserView? = null,
    val error: Exception? = null
)