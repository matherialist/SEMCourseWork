package com.holeaf.mobile.ui.shop

import android.graphics.Bitmap

class ShopItemUi(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val photo: Bitmap?,
    var amount: Int
)
