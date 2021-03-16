package com.holeaf.mobile.data.chat

import com.holeaf.mobile.ChatMessage
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.data.login.Result
import java.io.IOException

class ChatRepository(val dataSource: ChatDataSource) {
    suspend fun getMessages(chatId: Int): Result<List<ChatMessage>> {
        return dataSource.getMessages(chatId)
    }
}

class ChatDataSource {
    suspend fun getMessages(chatId: Int): Result<List<ChatMessage>> {
        try {
            MainApplication.token?.let {
                val messages =
                    MainApplication.getServiceAuth().getMessages(chatId)
                return Result.Success(messages)
            }
            return Result.Error(IOException("User isn't authorized"))
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting messages", e))
        }
    }
}