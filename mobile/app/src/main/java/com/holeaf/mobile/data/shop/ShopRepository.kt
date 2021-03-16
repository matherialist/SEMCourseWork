package com.holeaf.mobile.data.chat

import android.util.Log
import com.holeaf.mobile.CartItemData
import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.OrderData
import com.holeaf.mobile.data.login.Result
import com.holeaf.mobile.data.maps.model.PlaceData
import com.holeaf.mobile.data.orders.models.UserOrderData
import java.io.IOException

data class StoreItem(
    val id: Int,
    val title: String,
    val description: String?,
    val price: Int,
    val amount: Int,
    val photo: String
)

data class OrderDetailsItem(
    val itemId: Int,
    val amount: Int,
    val price: Int
)

data class OrderDetails(
    val orderId: Int,
    val clientId: Int,
    val totalCost: Float,
    val startDate: String,
    val closeDate: String,
    val long: Float,
    val lat: Float,
    val status: Int,
    val orderItemList: List<OrderDetailsItem>
)

class ShopRepository(val dataSource: ShopDataSource) {
    suspend fun getShop(): Result<List<StoreItem>> {
        return dataSource.getStoreItems()
    }

    suspend fun getCart(): Result<List<CartItemData>> {
        return dataSource.getCart()
    }

    suspend fun addToCart(itemData: CartItemData): Boolean {
        return dataSource.addToCart(itemData)
    }

    suspend fun removeFromCart(itemId: Int): Boolean {
        return dataSource.removeFromCart(itemId)
    }

    suspend fun createOrder(deliveryPosition: PlaceData): Boolean {
        return dataSource.createOrder(deliveryPosition)
    }

    suspend fun getOrders(): Result<List<UserOrderData>> {
        return dataSource.getOrders()
    }

    suspend fun getCourierAvailableOrders(): Result<List<UserOrderData>> {
        return dataSource.getCourierAvailableOrders()
    }

    suspend fun getOrderDetails(id: Int): Result<OrderData> {
        return dataSource.getOrderDetails(id)
    }

    suspend fun setStatus(order: Int, id: Int): Result<String> {
        return dataSource.setStatus(order, id)
    }
}

class ShopDataSource {

    suspend fun getStoreItems(): Result<List<StoreItem>> {
        try {
            val sAuth = MainApplication.getServiceAuth()
            return Result.Success(sAuth.getStore())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting store items", e))
        }
    }

    suspend fun getOrderDetails(id: Int): Result<OrderData> {
        try {
            val sAuth = MainApplication.getServiceAuth()
            return Result.Success(sAuth.getOrderDetails(id))
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting order details", e))
        }
    }

    suspend fun getOrders(): Result<List<UserOrderData>> {
        try {
            val sAuth = MainApplication.getServiceAuth()
            return Result.Success(sAuth.getOrders())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting orders", e))
        }
    }

    suspend fun getCourierAvailableOrders(): Result<List<UserOrderData>> {
        try {
            val sAuth = MainApplication.getServiceAuth()
            return Result.Success(sAuth.getCourierAvailableOrders())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting orders", e))
        }
    }

    suspend fun setStatus(order: Int, status: Int): Result<String> {
        try {
            val sAuth = MainApplication.getServiceAuth()
            return Result.Success(sAuth.setStatus(order, status))
        } catch (e: Throwable) {
            return Result.Error(IOException("Error setting status", e))
        }
    }

    suspend fun getCart(): Result<List<CartItemData>> {
        try {
            val sAuth = MainApplication.getServiceAuth()
            return Result.Success(sAuth.getCart())
        } catch (e: Throwable) {
            return Result.Error(IOException("Error getting cart items", e))
        }
    }

    suspend fun addToCart(itemData: CartItemData): Boolean {
        return try {
            val sAuth = MainApplication.getServiceAuth()
            Log.i("CART", "Add to cart: $itemData")
            sAuth.addToCart(itemData)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun removeFromCart(itemId: Int): Boolean {
        return try {
            val sAuth = MainApplication.getServiceAuth()
            Log.i("CART", "Delete from cart: $itemId")
            sAuth.deleteFromCart(itemId)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    suspend fun createOrder(deliveryPosition: PlaceData): Boolean {
        return try {
            val sAuth = MainApplication.getServiceAuth()
            Log.i(
                "CART",
                "Creating order at position: ${deliveryPosition.lat}:${deliveryPosition.long}"
            )
            sAuth.createOrder(deliveryPosition)
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

}

