package com.holeaf.mobile.ui.chat

import android.graphics.Bitmap
import android.view.View
import androidx.recyclerview.widget.RecyclerView


abstract class MessageViewHolder<in T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(avatar: Bitmap, item: T)
}