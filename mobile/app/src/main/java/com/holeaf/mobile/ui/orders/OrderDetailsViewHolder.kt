package com.holeaf.mobile.data.shop

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.OrderItem
import com.holeaf.mobile.R

class OrderDetailsViewHolder(
    val view: View
) :
    RecyclerView.ViewHolder(view) {
    private val orderDetailsItemName = view.findViewById<TextView>(R.id.order_details_item_name)
    private val orderDetailsItemPrice = view.findViewById<TextView>(R.id.order_details_item_price)
    private val orderDetailsItemImage = view.findViewById<ImageView>(R.id.order_details_item_image)

    fun bind(item: OrderItem) {
        orderDetailsItemName.text = item.name.orEmpty()
        orderDetailsItemPrice.text = "${item.price} р."
        if (item.photo != null) {
            orderDetailsItemImage.setImageBitmap(item.photo)
        } else {
            orderDetailsItemImage.setImageResource(R.drawable.question_mark)
        }
    }
}