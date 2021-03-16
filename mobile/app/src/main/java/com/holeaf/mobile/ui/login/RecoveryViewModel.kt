package com.holeaf.mobile.ui.recovery

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holeaf.mobile.R
import com.holeaf.mobile.data.login.RecoveryRepository
import com.holeaf.mobile.data.login.Result
import com.holeaf.mobile.ui.login.RecoveryFormState
import com.holeaf.mobile.data.login.model.RecoveryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RecoveryViewModel(private val recoveryRepository: RecoveryRepository) : ViewModel() {
//    val idlingResource = CountingIdlingResource("login")
//
//    init {
//        IdlingRegistry.getInstance().register(idlingResource)
//    }
    private val _recoveryForm = MutableLiveData<RecoveryFormState>()
    val recoveryFormState: LiveData<RecoveryFormState> = _recoveryForm

    private val _recoveryResult = MutableLiveData<RecoveryResult>()
    val recoveryResult: LiveData<RecoveryResult> = _recoveryResult

    fun recovery(email: String) {
        //idlingResource.increment()
        // can be launched in a separate asynchronous job
        GlobalScope.launch(Dispatchers.IO) {
            val result = recoveryRepository.recovery(email)

            GlobalScope.launch(Dispatchers.Main) {
                if (result is Result.Success) {
                    _recoveryResult.value = RecoveryResult(success = true)
                } else {
                    _recoveryResult.value = RecoveryResult(error = R.string.recovery_failed)
                }
            }
      //      idlingResource.decrement()
        }

    }

    fun recoveryDataChanged(email: String) {
        if (!isEmailValid(email)) {
            _recoveryForm.value = RecoveryFormState(emailError = R.string.email_is_required)
        } else {
            _recoveryForm.value = RecoveryFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isEmailValid(email: String): Boolean {
        return email.contains('@') && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}