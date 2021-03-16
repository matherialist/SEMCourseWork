package com.holeaf.mobile.ui.login

/**
 * Data validation state of the login form.
 */
data class RegistrationFormState(val usernameError: Int? = null,
                                 val emailError: Int? = null,
                                 val passwordError: Int? = null,
                                 val password2Error: Int? = null,
                                 val isDataValid: Boolean = false)