package com.holeaf.mobile.data.login

import com.holeaf.mobile.AuthUser
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.NewUser
import com.holeaf.mobile.data.login.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class RegistrationDataSource {

    suspend fun register(username: String, password: String, email: String): Result<LoggedInUser> {
        try {
            MainApplication.getService().registerUser(NewUser(username, password, email))
            val token = MainApplication.getService().authUser(AuthUser(username, password))
            //user is registered
            return Result.Success(LoggedInUser(token.token, token.id, username, token.permissions))
        } catch (e: Throwable) {
            e.printStackTrace()
            return Result.Error(IOException("Ошибка при регистрации", e))
        }
    }
}