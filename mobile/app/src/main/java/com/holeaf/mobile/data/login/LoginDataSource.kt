package com.holeaf.mobile.data.login

import com.holeaf.mobile.AuthResult
import com.holeaf.mobile.AuthUser
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.data.login.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    val TAG = LoginDataSource::class.java.simpleName

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            val authResult: AuthResult =
                MainApplication.getService().authUser(AuthUser(username, password))
            //user is logged in
            return Result.Success(LoggedInUser(authResult.token, authResult.id, username, authResult.permissions))
        } catch (e: Throwable) {
            e.printStackTrace()
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}