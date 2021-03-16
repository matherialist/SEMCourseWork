package com.holeaf.mobile.data.login

import com.holeaf.mobile.MainApplication
import java.io.IOException

//@Serializable
data class UserProfileServerInfo(
    val id: Int,
    val user: String,
    val email: String,
    val role: Int,
    val photo: String
)

data class UserProfileInfo(
    val id: Int,
    val user: String,
    val email: String,
    val rolename: String,
    val photo: String
)

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class ProfileDataSource {

    suspend fun getInformation(): Result<UserProfileInfo> {
        try {
            val info = MainApplication.getServiceAuth().getUser()
            val roles = MainApplication.getServiceAuth().getRoles()
            val name = roles.firstOrNull { it.id == info.role }?.name ?: "Неизвестно"
            val nobj = UserProfileInfo(info.id, info.user, info.email, name, info.photo)
            return Result.Success(nobj)
        } catch (e: Throwable) {
            e.printStackTrace()
            return Result.Error(IOException("Error getting information", e))
        }
    }
}