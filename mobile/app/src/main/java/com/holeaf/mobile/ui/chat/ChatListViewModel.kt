package com.holeaf.mobile.ui.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.data.chat.ChatListRepository
import com.holeaf.mobile.data.login.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ChatListViewModel(val chatListRepository: ChatListRepository) : ViewModel() {
    var chatListData = MutableLiveData<MutableList<ChatListItemUi>>()

    var myAvatar = MutableLiveData<String>()

    fun update() {
        GlobalScope.launch(Dispatchers.IO) {
            val avatar = chatListRepository.getProfile()
            if (avatar is Result.Success) {
                myAvatar.postValue(avatar.data.photo)
            }
            val roles = chatListRepository.getRoles()
            val result = chatListRepository.getChatList()

            if (result is Result.Success && roles is Result.Success) {
                val roleDescriptors = roles.data
                chatListData.postValue(result.data.filter { it.id != MainApplication.id }.map {
                    ChatListItemUi(
                        it.id,
                        it.login,
                        it.last,
                        it.avatar,
                        roleDescriptors.firstOrNull { fo -> fo.id == it.roleId }
                    )
                }.toMutableList())
            }
        }
    }

}