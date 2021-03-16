package com.holeaf.mobile.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.Role
import com.holeaf.mobile.data.chat.ChatListAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatListFragment : Fragment() {

    val viewModel: ChatListViewModel by viewModel()

    fun click(id: Int, name: String, avatar: String, myAvatar: String, role: Role) {
        requireActivity().supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            ChatFragment.newInstance(id, name, avatar, myAvatar, role)
        ).addToBackStack(null).commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    private lateinit var chatListAdapter: ChatListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chats = view.findViewById<RecyclerView>(R.id.chats)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            .apply {
                isSmoothScrollbarEnabled = true
            }
        viewModel.update()
        viewModel.chatListData.observe(viewLifecycleOwner) {
            chatListAdapter =
                ChatListAdapter(mutableListOf(), viewModel.myAvatar.value.orEmpty(), ::click)
            chatListAdapter.data = viewModel.chatListData.value.orEmpty().toMutableList()
            chats.layoutManager = layoutManager
            chats.adapter = chatListAdapter
        }
    }
}