package com.holeaf.mobile.ui.orders

import android.app.Activity
import android.util.DisplayMetrics
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.holeaf.mobile.R
import com.holeaf.mobile.data.login.model.LoggedUserInfo
import com.holeaf.mobile.data.orders.OrderItemsAdapter
import com.holeaf.mobile.data.orders.models.UserOrderData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class OrderListViewHolder(
    val activity: Activity,
    val view: View,
    val click: (id: Int) -> Unit
) :
    RecyclerView.ViewHolder(view), KoinComponent {

    private val title: TextView = view.findViewById(R.id.order_item_title)
    private val items: ListView = view.findViewById(R.id.order_list_item_elements)
    private val summaryCost: TextView = view.findViewById(R.id.order_summary_cost)

    val loggedUserInfo: LoggedUserInfo by inject()

    fun bind(userOrder: UserOrderData) {


        val status = if (loggedUserInfo.role?.mapClient == true) {
            when (userOrder.status) {
                0 -> "ожидает доставки"
                1 -> "передан в доставку"
                2 -> "завершен"
                3 -> "отменен"
                else -> "?"
            }
        } else if (loggedUserInfo.role?.mapCourier == true) {
            when (userOrder.status) {
                0 -> "новый"
                1 -> "доставка"
                2 -> "завершен"
                3 -> "отменен"
                else -> "?"
            }
        } else {
            "?"
        }

        title.text = "Заказ №${userOrder.id} ($status)"
        val dp = 88 * userOrder.items.size
        items.layoutParams.height = dp * (activity.getResources()
            .getDisplayMetrics().densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT).toInt()
        items.adapter = OrderItemsAdapter(activity, userOrder.id, userOrder.items) {
            click(userOrder.id)
        }
        summaryCost.text = "${userOrder.totalCost} р."
        view.setOnClickListener {
            click(userOrder.id)
        }
    }
}

