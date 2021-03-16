package com.holeaf.mobile.data.shop

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.ui.shop.ShopItemUi

class ShopViewHolder(val view: View, val click: (id: Int, delta: Int) -> Int) :
    RecyclerView.ViewHolder(view) {
    private val shopItemName = view.findViewById<TextView>(R.id.item_name)
    private val shopItemDescription = view.findViewById<TextView>(R.id.item_description)
    private val shopItemPrice = view.findViewById<TextView>(R.id.item_price)
    private val shopItemImage = view.findViewById<ImageView>(R.id.item_image)
    private val shopMinus = view.findViewById<Button>(R.id.item_minus)
    private val shopPlus = view.findViewById<Button>(R.id.item_plus)
    private val shopItemCount = view.findViewById<TextView>(R.id.item_count)

    fun bind(item: ShopItemUi) {
        shopItemName.text = item.name
        shopItemDescription.text = item.description
        shopItemPrice.text = "${item.price} р."
        shopItemCount.text = item.amount.toString()
        if (item.photo != null) {
            shopItemImage.setImageBitmap(item.photo)
        } else {
            shopItemImage.setImageResource(R.drawable.question_mark)
        }
        shopMinus.setOnClickListener {
            val newCount = click(item.id, -1)
            shopItemCount.text = "$newCount"
        }
        shopPlus.setOnClickListener {
            val newCount = click(item.id, 1)
            shopItemCount.text = "$newCount"
        }
        //todo plus-minus buttons

    }
}