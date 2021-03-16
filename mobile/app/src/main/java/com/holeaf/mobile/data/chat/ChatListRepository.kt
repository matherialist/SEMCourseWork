package com.holeaf.mobile.data.chat

import com.holeaf.mobile.ChatInfo
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.Role
import com.holeaf.mobile.data.login.Result
import com.holeaf.mobile.data.login.UserProfileServerInfo
import java.io.IOException

class ChatListRepository(val dataSource: ChatListDataSource) {
    suspend fun getChatList(): Result<List<ChatInfo>> {
        return dataSource.getChatsList()
    }

    suspend fun getProfile(): Result<UserProfileServerInfo> {
        return dataSource.getUserProfile()
    }

    suspend fun getRoles(): Result<List<Role>> {
        return dataSource.getRoles()
    }
}

class ChatListDataSource {

    suspend fun getChatsList(): Result<List<ChatInfo>> {
        try {
            val sAuth = MainApplication.getServiceAuth()
            return Result.Success(sAuth.getUserChats())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting chats", e))
        }
    }

    suspend fun getUserProfile(): Result<UserProfileServerInfo> {
        try {
            val sAuth = MainApplication.getServiceAuth()
            return Result.Success(sAuth.getUser())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting profile", e))
        }
    }

    suspend fun getRoles(): Result<List<Role>> {
        try {
            val sAuth = MainApplication.getServiceAuth()
            return Result.Success(sAuth.getRoles())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting roles", e))
        }
    }
}

