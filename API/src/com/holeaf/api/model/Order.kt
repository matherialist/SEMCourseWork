package com.holeaf.api.model

import com.holeaf.api.API.OrderItemList

data class UserOrderData(var id: Int, var totalCost: Int, var openDate: String, var closeDate: String,
                         var status: Int, var lat: Float, var long: Float,
                         var courierName:String, var courierPhoto:String,
                         val items: List<OrderItemList>
)

data class OrderListData(var itemId: Int, var amount: Int, var price: Int, var orderId: Int)
