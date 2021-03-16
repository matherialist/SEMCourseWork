package com.holeaf.mobile.ui.login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.R
import com.holeaf.mobile.data.login.LoginRepository
import com.holeaf.mobile.data.login.model.LoginResult
import com.holeaf.mobile.data.login.Result
import com.holeaf.mobile.data.login.model.LoggedUserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel(), KoinComponent {

    val loggedUserInfo: LoggedUserInfo by inject()

    val idlingResource = CountingIdlingResource("login")

    init {
        IdlingRegistry.getInstance().register(idlingResource)
    }

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _loginProgress = MutableLiveData(false)
    val loginProgress: LiveData<Boolean> = _loginProgress


    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
        idlingResource.increment()
        GlobalScope.launch(Dispatchers.IO) {
            _loginProgress.postValue(true)
            val result = loginRepository.login(username, password)

            GlobalScope.launch(Dispatchers.Main) {
                if (result is Result.Success) {

                    Log.i("Wait", "Waiting for login")
                    MainApplication.globalLocker.increment()
                    _loginResult.value =
                        LoginResult(
                            success = LoggedInUserView(
                                displayName = result.data.displayName,
                                result.data.permissions
                            )
                        )
                    loggedUserInfo.username = result.data.displayName
                    loggedUserInfo.role = result.data.permissions
                } else {
                    _loginResult.value = LoginResult(error = R.string.login_failed)
                }
            }
            _loginProgress.postValue(false)
            idlingResource.decrement()
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        if (username.isBlank()) return true
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length == 0 || password.length > 5
    }

//    init {
//         login("test1", "12345678")
//    }
}