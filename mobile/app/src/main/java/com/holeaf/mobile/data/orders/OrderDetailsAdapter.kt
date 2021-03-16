package com.holeaf.mobile.data.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.OrderItem
import com.holeaf.mobile.R
import com.holeaf.mobile.data.shop.OrderDetailsViewHolder

class OrderDetailsAdapter:
    RecyclerView.Adapter<OrderDetailsViewHolder>() {

    var items: MutableList<OrderItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val context = parent.context
        return OrderDetailsViewHolder(
            LayoutInflater.from(context).inflate(R.layout.order_details_product_info, parent, false)
        )
    }

    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = 0
}
