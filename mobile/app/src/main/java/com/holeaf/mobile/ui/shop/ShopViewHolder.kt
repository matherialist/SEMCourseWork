package com.holeaf.mobile.data.shop

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.ui.shop.CartItem
import com.holeaf.mobile.ui.shop.ShopItemUi

class CartViewHolder(
    val items: List<ShopItemUi>,
    val view: View,
    val click: (id: Int, delta: Int) -> Int
) :
    RecyclerView.ViewHolder(view) {
    private val cartItemName = view.findViewById<TextView>(R.id.short_item_name)
    private val cartItemPrice = view.findViewById<TextView>(R.id.short_item_price)
    private val cartItemImage = view.findViewById<ImageView>(R.id.short_item_image)
    private val cartMinus = view.findViewById<Button>(R.id.short_item_minus)
    private val cartPlus = view.findViewById<Button>(R.id.short_item_plus)
    private val cartItemCount = view.findViewById<TextView>(R.id.short_item_count)

    fun bind(item: CartItem) {
        val shopItem = items.firstOrNull { it.id == item.id }
        if (shopItem != null) {
            cartItemName.text = shopItem.name
            cartItemPrice.text = "${shopItem.price} р."
            cartItemCount.text = item.amount.toString()
            if (shopItem.photo != null) {
                cartItemImage.setImageBitmap(shopItem.photo)
            } else {
                cartItemImage.setImageResource(R.drawable.question_mark)
            }
            cartMinus.setOnClickListener {
                val newCount = click(shopItem.id, -1)
                cartItemCount.text = "$newCount"
            }
            cartPlus.setOnClickListener {
                val newCount = click(shopItem.id, 1)
                cartItemCount.text = "$newCount"
            }
        } else {
            cartItemName.text = ""
            cartItemPrice.text = ""
            cartItemCount.text = ""
            cartItemImage.setImageResource(0)
        }
    }
}