package com.holeaf.mobile.data.login

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class RecoveryRepository(val dataSource: RecoveryDataSource) {
    suspend fun recovery(email: String): Result<Boolean> {
        val result = dataSource.recovery(email)
        return result
    }
}