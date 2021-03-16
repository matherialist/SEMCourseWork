package com.holeaf.api.model

data class SupplyContractData(var id: Int, var totalCost: Int, var openDate: String, var closeDate: String, var status:Int)

data class SupplyListData(var itemId: Int, var amount: Int, var price: Float, var contractId: Int)