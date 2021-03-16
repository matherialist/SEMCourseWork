package com.holeaf.mobile.ui.chat

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.holeaf.mobile.R

class MyMessageViewHolder(val view: View) : MessageViewHolder<MessageItemUi>(view) {
    private val messageContent = view.findViewById<TextView>(R.id.message)
    private val myAvatar = view.findViewById<ImageView>(R.id.my_avatar)

    override fun bind(avatar: Bitmap, item: MessageItemUi) {
        messageContent.text = item.content
        messageContent.setTextColor(item.textColor)
        myAvatar.setImageBitmap(avatar)
    }
}

class InterlocutorMessageViewHolder(val view: View) : MessageViewHolder<MessageItemUi>(view) {
    private val messageContent = view.findViewById<TextView>(R.id.message)
    private val chatAvatar = view.findViewById<ImageView>(R.id.interlocutor_avatar)

    override fun bind(avatar: Bitmap, item: MessageItemUi) {
        messageContent.text = item.content
        messageContent.setTextColor(item.textColor)
        chatAvatar.setImageBitmap(avatar)
    }
}