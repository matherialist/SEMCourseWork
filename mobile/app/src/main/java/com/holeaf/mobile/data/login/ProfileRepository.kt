package com.holeaf.mobile.data.login

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class ProfileRepository(val dataSource: ProfileDataSource) {
    suspend fun getUserInfo(): Result<UserProfileInfo> {
        return dataSource.getInformation()
    }
}