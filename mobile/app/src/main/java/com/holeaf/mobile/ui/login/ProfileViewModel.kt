package com.holeaf.mobile.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import com.holeaf.mobile.data.login.ProfileRepository
import com.holeaf.mobile.data.login.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

data class UserProfile(
    val username: String,
    val email: String,
    val rolename: String,
    val photo: String
)

data class ProfileResult(
    val success: UserProfile? = null,
    val error: Int? = null
)

class ProfileViewModel(private val profileRepository: ProfileRepository) : ViewModel() {
    var idlingResource: CountingIdlingResource = CountingIdlingResource("profile")

    init {
        IdlingRegistry.getInstance().register(idlingResource)
    }

    var _profileResult = MutableLiveData<ProfileResult>()
    val profileResult: LiveData<ProfileResult> = _profileResult

    fun load() {

        idlingResource.increment()
        GlobalScope.launch(Dispatchers.IO) {
            var result = profileRepository.getUserInfo()
            //GlobalScope.launch(Dispatchers.Main) {
            if (result is Result.Success) {
                _profileResult.postValue(
                    ProfileResult(
                        success = UserProfile(
                            result.data.user,
                            result.data.email,
                            result.data.rolename,
                            result.data.photo
                        )
                    )
                )

            } else {
                _profileResult.postValue(ProfileResult(error = 1))
            }
            idlingResource.decrement()
        }
    }
}
