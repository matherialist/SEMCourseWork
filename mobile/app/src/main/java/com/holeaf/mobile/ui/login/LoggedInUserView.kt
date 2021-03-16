package com.holeaf.mobile.ui.login

import com.holeaf.mobile.Role

data class LoggedInUserView(
    val displayName: String,
    val permissions: Role
)