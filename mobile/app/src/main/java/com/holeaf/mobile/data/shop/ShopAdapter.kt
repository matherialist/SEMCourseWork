package com.holeaf.mobile.data.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.data.shop.ShopViewHolder
import com.holeaf.mobile.ui.shop.ShopItemUi


class ShopAdapter(
    val click: (id: Int, delta: Int) -> Int
) :
    RecyclerView.Adapter<ShopViewHolder>() {

    var data: MutableList<ShopItemUi> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val context = parent.context
        return ShopViewHolder(
            LayoutInflater.from(context).inflate(R.layout.product_info, parent, false), click
        )
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = 0
}
