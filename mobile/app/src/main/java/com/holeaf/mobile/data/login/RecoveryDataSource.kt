package com.holeaf.mobile.data.login

import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.RecoveryUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class RecoveryDataSource {

    suspend fun recovery(email: String): Result<Boolean> {
        try {
            MainApplication.getService().recovery(RecoveryUser(email))
            //successfull recovered
            return Result.Success(true)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error in password recovery", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}