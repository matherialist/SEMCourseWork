package com.holeaf.mobile.ui.chat

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.Role
import com.holeaf.mobile.ui.login.decodePhoto

class ChatListViewHolder(
    val view: View,
    val context: Context,
    val myAvatar: String,
    val click: (id: Int, name: String, avatar: String, myAvatar: String, role: Role) -> Unit
) :
    RecyclerView.ViewHolder(view) {
    private val chatContainer = view.findViewById<ConstraintLayout>(R.id.chat_list_item_container)
    private val chatNameContent = view.findViewById<TextView>(R.id.chatname)
    private val lastMessageContent = view.findViewById<TextView>(R.id.last_message)
    private val avatar = view.findViewById<ImageView>(R.id.interlocutor_avatar)

    fun bind(item: ChatListItemUi) {
        chatNameContent.text = item.name
        lastMessageContent.text = item.last
        var chatAvatar: Bitmap

        chatAvatar = decodePhoto(item.avatar, item.role, context)
        avatar.setImageBitmap(chatAvatar)
        chatContainer.setOnClickListener {
            click(
                item.id,
                item.name,
                item.avatar,
                myAvatar,
                item.role ?: Role(
                    0,
                    "client",
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false
                )
            )
        }
    }
}
