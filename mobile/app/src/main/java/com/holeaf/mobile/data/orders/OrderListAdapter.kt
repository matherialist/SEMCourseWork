package com.holeaf.mobile.data.orders

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.data.orders.models.UserOrderData
import com.holeaf.mobile.ui.orders.OrderListViewHolder


class OrderListAdapter(
    val activity: Activity,
    val click: (id: Int) -> Unit
) :
    RecyclerView.Adapter<OrderListViewHolder>() {

    var data: MutableList<UserOrderData> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderListViewHolder {
        val context = parent.context
        return OrderListViewHolder(
            activity,
            LayoutInflater.from(context).inflate(R.layout.order_list_item, parent, false), click
        )
    }

    override fun onBindViewHolder(holder: OrderListViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = 0
}
