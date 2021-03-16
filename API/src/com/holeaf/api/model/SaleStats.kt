package com.holeaf.api.model

data class ProductSaleStats(val itemId: Int, val itemName:String, var totalCost: Int, var amount: Int)

data class DailySaleStats(var totalCost:Int, val date:String)
