package com.holeaf.mobile.data.chat

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.ui.chat.InterlocutorMessageViewHolder
import com.holeaf.mobile.ui.chat.MessageItemUi
import com.holeaf.mobile.ui.chat.MessageItemUi.Companion.TYPE_INTERLOCUTOR_MESSAGE
import com.holeaf.mobile.ui.chat.MessageItemUi.Companion.TYPE_MY_MESSAGE
import com.holeaf.mobile.ui.chat.MessageViewHolder
import com.holeaf.mobile.ui.chat.MyMessageViewHolder


class ChatAdapter(
    var data: MutableList<MessageItemUi>,
    val myAvatar: Bitmap,
    val chatAvatar: Bitmap
) : RecyclerView.Adapter<MessageViewHolder<*>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder<*> {
        val context = parent.context
        return when (viewType) {
            TYPE_MY_MESSAGE -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.my_message_item, parent, false)
                MyMessageViewHolder(view)
            }
            TYPE_INTERLOCUTOR_MESSAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.interlocutor_message_item, parent, false)
                InterlocutorMessageViewHolder(
                    view
                )
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder<*>, position: Int) {
        val item = data[position]
        Log.d("adapter View", position.toString() + item.content)
        when (holder) {
            is MyMessageViewHolder -> holder.bind(myAvatar, item)
            is InterlocutorMessageViewHolder -> holder.bind(chatAvatar, item)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = data[position].messageType
}
