package com.holeaf.mobile.ui.login

/**
 * Data validation state of the login form.
 */
data class RecoveryFormState(
    val emailError: Int? = null,
    val isDataValid: Boolean = false
)