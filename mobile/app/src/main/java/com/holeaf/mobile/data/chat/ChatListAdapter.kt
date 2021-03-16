package com.holeaf.mobile.data.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.Role
import com.holeaf.mobile.ui.chat.ChatListItemUi
import com.holeaf.mobile.ui.chat.ChatListViewHolder


class ChatListAdapter(
    var data: MutableList<ChatListItemUi>,
    val myAvatar: String,
    val click: (id: Int, name: String, chatAvatar: String, myAvatar: String, role: Role) -> Unit
) :
    RecyclerView.Adapter<ChatListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val context = parent.context
        return ChatListViewHolder(
            LayoutInflater.from(context).inflate(R.layout.chat_list_item, parent, false),
            context,
            myAvatar,
            click
        )
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = 0
}
