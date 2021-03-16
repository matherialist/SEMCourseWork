package com.holeaf.api.model

data class CartItemData(var userId: Int, var itemId: Int, var amount: Int)

data class CartItem(var itemId: Int)