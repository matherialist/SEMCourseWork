package com.holeaf.mobile.data.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.data.shop.CartViewHolder
import com.holeaf.mobile.ui.shop.CartItem
import com.holeaf.mobile.ui.shop.ShopItemUi


class CartAdapter(
    val click: (id: Int, delta: Int) -> Int
) :
    RecyclerView.Adapter<CartViewHolder>() {

    var items: MutableList<ShopItemUi> = mutableListOf()
    var data: MutableList<CartItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val context = parent.context
        return CartViewHolder(
            items,
            LayoutInflater.from(context).inflate(R.layout.short_product_info, parent, false), click
        )
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = 0
}
