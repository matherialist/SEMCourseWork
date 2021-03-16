package com.holeaf.mobile.data.orders

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.holeaf.mobile.OrderItem
import com.holeaf.mobile.R

class OrderItemsAdapter(
    val context: Activity,
    val id: Int,
    val items: List<OrderItem>,
    val click: (id: Int) -> Unit
) :
    ArrayAdapter<String>(context, R.layout.order_product_info) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val row = inflater.inflate(R.layout.order_product_info, null, true)

        val itemName: TextView = row.findViewById(R.id.order_item_name)
        val amount: TextView = row.findViewById(R.id.order_item_amount_info)
        val price: TextView = row.findViewById(R.id.order_item_price)
        val photo: ImageView = row.findViewById(R.id.order_item_image)

        val d = items?.get(position)
        itemName.text = d?.name.orEmpty()
        amount.text = d?.amount?.toString()
        price.text = "${d?.price?.toString()} р."
        d?.photo?.let {
            photo.setImageBitmap(it)
        }
        row.setOnClickListener {
            click(id ?: 0)
        }

        return row
    }

    override fun getCount(): Int {
        return items.size
    }
}
