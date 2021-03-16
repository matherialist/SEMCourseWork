package com.holeaf.mobile.ui.chat

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holeaf.mobile.ChatMessage
import com.holeaf.mobile.ChatService
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.NewMessage
import com.holeaf.mobile.data.chat.ChatAdapter
import com.holeaf.mobile.data.chat.ChatRepository
import com.holeaf.mobile.data.login.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ChatViewModel(val chatRepository: ChatRepository) : ViewModel() {

    var chatId: Int? = null
    var chatName: String? = null
    var chatService: ChatService? = null

    var cachedChats: MutableMap<Int, ChatService> = mutableMapOf()

    var messagesData = MutableLiveData<MutableList<MessageItemUi>>()
    var notifyNewMessageInsertedLiveData = MutableLiveData<Int>()

    fun loadMessages(chatAdapter: ChatAdapter, next: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            chatId?.let {
                val result: Result<List<ChatMessage>> = chatRepository.getMessages(it)
                if (result is Result.Success) {
                    messagesData.postValue(mutableListOf())
                    if (result.data.isEmpty()) {
                        chatAdapter.data = mutableListOf()
                        notifyNewMessageInsertedLiveData.postValue(0)
                        next()
                    }
                    result.data.forEach {
                        Log.i("ChatVM", "Message is $it")
                        messagesData.value?.add(
                            MessageItemUi(
                                it.content,
                                Color.BLACK,
                                if (it.to == chatId) MessageItemUi.TYPE_MY_MESSAGE else MessageItemUi.TYPE_INTERLOCUTOR_MESSAGE
                            )
                        )
                    }
                    chatAdapter.data = messagesData.value.orEmpty().toMutableList()
                    notifyNewMessageInsertedLiveData.postValue(messagesData.value.orEmpty().size)
                    next()
                } else {
                    Log.i("ChatVM", "Error occured: " + (result as Result.Error).exception)
                }
            }
        }
        MainApplication.token?.let { token ->
            chatId?.let { chatId ->
                if (!cachedChats.containsKey(chatId)) {
                    chatService = MainApplication.instantiateChat(chatId, {
                        Log.i("ChatVM", "Message accepted $it")
                        if (it.to != 0) {
                            messagesData.value?.add(
                                MessageItemUi(
                                    it.content,
                                    Color.BLACK,
                                    MessageItemUi.TYPE_INTERLOCUTOR_MESSAGE
                                )
                            )
                            notifyNewMessageInsertedLiveData.postValue(messagesData.value.orEmpty().size)
                        }
                    })
                    chatService?.let {
                        cachedChats[chatId] = it
                    }
                } else {
                    chatService = cachedChats[chatId]
                }
            }
        }
    }

    fun sendMessage(message: String) {
        Log.i("ChatID", "$chatId")
        val newMessage = chatId?.let { NewMessage(it, message) }
        Log.i("ChatVM", "Sending message $newMessage")
        newMessage?.let {
            messagesData.value?.add(
                MessageItemUi(
                    newMessage.content,
                    Color.BLACK,
                    MessageItemUi.TYPE_MY_MESSAGE
                )
            )
            notifyNewMessageInsertedLiveData.postValue(messagesData.value.orEmpty().size)
            chatService?.sendMessage(newMessage)
        }
    }
}
