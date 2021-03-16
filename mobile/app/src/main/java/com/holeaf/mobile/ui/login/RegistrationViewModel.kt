package com.holeaf.mobile.ui.registration

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.R
import com.holeaf.mobile.data.login.RegistrationRepository
import com.holeaf.mobile.data.login.Result
import com.holeaf.mobile.ui.login.LoggedInUserView
import com.holeaf.mobile.ui.login.RegistrationFormState
import com.holeaf.mobile.data.login.model.RegistrationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegistrationViewModel(private val registrationRepository: RegistrationRepository) :
    ViewModel() {
    val idlingResource = CountingIdlingResource("login")

    init {
        IdlingRegistry.getInstance().register(idlingResource)
    }

    private val _registrationForm = MutableLiveData<RegistrationFormState>()
    val registrationFormState: LiveData<RegistrationFormState> = _registrationForm

    private val _registrationResult = MutableLiveData<RegistrationResult>()
    val registrationResult: LiveData<RegistrationResult> = _registrationResult

    fun register(username: String, email: String, password: String) {
        idlingResource.increment()
        GlobalScope.launch(Dispatchers.IO) {
            val result = registrationRepository.register(username, email, password)

            GlobalScope.launch(Dispatchers.Main) {
                if (result is Result.Success) {
                    _registrationResult.value = RegistrationResult(
                        success = LoggedInUserView(
                            displayName = result.data.displayName,
                            result.data.permissions
                        )
                    )
                    MainApplication.authenticate(result.data.userId, result.data.id)
                } else {
                    _registrationResult.value =
                        RegistrationResult(error = (result as Result.Error).exception)
                }
            }
            idlingResource.decrement()
        }
    }

    fun registrationDataChanged(
        username: String,
        password: String,
        password2: String,
        email: String
    ) {
        if (!isUserNameValid(username)) {
            _registrationForm.value =
                RegistrationFormState(usernameError = R.string.invalid_username)
        } else if (!isEmailValid(email)) {
            _registrationForm.value = RegistrationFormState(emailError = R.string.error_in_email)
        } else if (!isPasswordValid(password)) {
            _registrationForm.value =
                RegistrationFormState(passwordError = R.string.invalid_password)
        } else if (password != password2) {
            _registrationForm.value =
                RegistrationFormState(password2Error = R.string.passwords_not_match)
        } else {
            _registrationForm.value = RegistrationFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return !username.contains('@') && username.isNotBlank()
    }

    private fun isEmailValid(username: String): Boolean {
        return username.contains('@') && Patterns.EMAIL_ADDRESS.matcher(username).matches()
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}