package com.holeaf.mobile.data.orders.models

import com.holeaf.mobile.OrderItem

data class UserOrderData(
    var id: Int,
    var totalCost: Int,
    var openDate: String,
    var closeDate: String,
    var courierName: String,
    var courierPhoto: String,
    var status: Int,
    var lat: Float,
    val long: Float,
    val items: List<OrderItem>
)
