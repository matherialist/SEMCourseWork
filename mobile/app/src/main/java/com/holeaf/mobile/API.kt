package com.holeaf.mobile

import android.graphics.Bitmap
import com.holeaf.mobile.data.chat.StoreItem
import com.holeaf.mobile.data.login.UserProfileServerInfo
import com.holeaf.mobile.data.maps.model.PlaceData
import com.holeaf.mobile.data.maps.model.PlaceInfo
import com.holeaf.mobile.data.orders.models.UserOrderData
import com.holeaf.mobile.ui.TelemetryPosition
import kotlinx.serialization.Serializable
import retrofit2.http.*

data class CartItemData(val itemId: Int, val amount: Int)

data class OrderInfo(
    val id: Int,
    val totalCost: Float,
    val openDate: String,
    val closeDate: String,
    val courierName: String,
    val courierPhoto: String,
    val status: Int
)

data class OrderItem(
    val itemId: Int,
    val amount: Int,
    val price: Int,
    var name: String?,
    var photo: Bitmap?
)

data class OrderData(
    val orderId: Int,
    val clientId: Int,
    val totalCost: Int,
    val startDate: String,
    val closeDate: String,
    val long: Float,
    val lat: Float,
    var status: Int,
    val orderItemList: List<OrderItem>
)

@Serializable
data class Role(
    val id: Int,
    val name: String,
    val chatEveryone: Boolean,
    val chatCourier: Boolean,
    val chatSupplier: Boolean,
    val chatManager: Boolean,
    val chatClient: Boolean,
    val chatSeller: Boolean,
    val mapClient: Boolean,
    val mapSeller: Boolean,
    val mapManager: Boolean,
    val mapCourier: Boolean,
    val storeClient: Boolean,
    val storeSeller: Boolean,
    val supplies: Boolean,
    val orders: Boolean,
    val telemetry: Boolean,
    val stats: Boolean,
    val keys: Boolean,
    val users: Boolean
)

@Serializable
data class AuthUser(val login: String, val password: String)

@Serializable
data class AuthResult(val token: String, val id: Int, val role: Int, val permissions: Role)

@Serializable
data class NewUser(val login: String, val password: String, val email: String)

@Serializable
data class RecoveryUser(val email: String)



@Serializable
data class ChatInfo(
    val id: Int,
    val login: String,
    val email: String,
    val last: String,
    val avatar: String,
    val roleId: Int
)

@Serializable
data class ChatMessage(val from: Int, val to: Int, val time: String, val content: String)

@Serializable
data class NewMessage(val to: Int, val content: String)

@Serializable
data class NewEvent(val content: String)

@Serializable
data class RegisterResult(
    var status: String? = null,
    var message: String? = null
)

interface HoleafService {
    @POST("auth")
    suspend fun authUser(@Body body: AuthUser): AuthResult

    @POST("user")
    suspend fun registerUser(@Body body: NewUser): RegisterResult

    @POST("user/reset")
    suspend fun recovery(@Body body: RecoveryUser): RegisterResult

    @GET("user/chats")
    suspend fun getUserChats(): List<ChatInfo>

    @GET("messages/{id}")
    suspend fun getMessages(@Path("id") id: Int): List<ChatMessage>

    @GET("user")
    suspend fun getUser(): UserProfileServerInfo

    @GET("roles")
    suspend fun getRoles(): List<Role>

    @GET("store")
    suspend fun getStore(): List<StoreItem>

    @GET("cart")
    suspend fun getCart(): List<CartItemData>

    @POST("cart")
    suspend fun addToCart(@Body item: CartItemData)

    @DELETE("cart/{id}")
    suspend fun deleteFromCart(@Path("id") id: Int)

    @POST("order")
    suspend fun createOrder(@Body place: PlaceData)

    @POST("place")
    suspend fun savePlace(@Body place: PlaceInfo): List<PlaceInfo>

    @GET("place/all")
    suspend fun getAllPlaces(): List<PlaceInfo>

    @GET("order")
    suspend fun getOrders(): List<UserOrderData>

    @GET("order/district")
    suspend fun getCourierAvailableOrders(): List<UserOrderData>

    @GET("order/{id}")
    suspend fun getOrderDetails(@Path("id") id: Int): OrderData

    @POST("order/{id}/{status}")
    suspend fun setStatus(@Path("id") id: Int, @Path("status") status: Int): String

    @GET("telemetry")
    suspend fun getTelemetryList(): List<Int>

    @GET("telemetry/{id}")
    suspend fun getTelemetry(@Path("id") id: Int): List<TelemetryPosition>
}