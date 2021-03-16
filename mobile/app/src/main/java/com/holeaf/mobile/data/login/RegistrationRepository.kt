package com.holeaf.mobile.data.login

import com.holeaf.mobile.data.login.model.LoggedInUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class RegistrationRepository(val dataSource: RegistrationDataSource) {
    suspend fun register(username: String, email: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.register(username, email, password)
        return result
    }
}