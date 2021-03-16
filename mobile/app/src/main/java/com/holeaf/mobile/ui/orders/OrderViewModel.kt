package com.holeaf.mobile.ui.orders

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import com.holeaf.mobile.OrderData
import com.holeaf.mobile.data.chat.ShopRepository
import com.holeaf.mobile.data.login.Result
import com.holeaf.mobile.data.login.model.LoggedUserInfo
import com.holeaf.mobile.data.orders.models.UserOrderData
import com.holeaf.mobile.ui.shop.ShopItemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class OrderViewModel(val shopRepository: ShopRepository) :
    ViewModel(), KoinComponent {

    val orders: MutableLiveData<List<UserOrderData>> = MutableLiveData()

    val loggedUserInfo: LoggedUserInfo by inject()

    val orderDetails: MutableLiveData<OrderData> = MutableLiveData()

    val idlingResource = CountingIdlingResource("Orders")

    init {
        IdlingRegistry.getInstance().register(idlingResource)
    }

    val shopData: MutableLiveData<List<ShopItemUi>> = MutableLiveData()

    suspend fun fillShopData() {
        val result = shopRepository.getShop()
        if (result is Result.Success) {
            shopData.postValue(
                result.data.map {
                    var photo: Bitmap? = null
                    try {
                        val ba = Base64.decode(it.photo, Base64.DEFAULT)
                        if (ba.size > 0) {
                            photo = BitmapFactory.decodeByteArray(ba, 0, ba.size)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        //pass
                    }
                    ShopItemUi(
                        it.id,
                        it.title,
                        it.description.orEmpty(),
                        it.price,
                        photo,
                        0
                    )
                }.toMutableList()
            )
        }
    }

    fun getDetails(id: Int) {
        idlingResource.increment()
        GlobalScope.launch(Dispatchers.IO) {
            fillShopData()
            val result = shopRepository.getOrderDetails(id)
            if (result is Result.Success) {
                result.data.orderItemList.forEach { oit ->
                    val shop = shopData.value
                    val product = shop?.firstOrNull { it.id == oit.itemId }
                    oit.name = product?.name
                    oit.photo = product?.photo
                }
                orderDetails.postValue(result.data)
            }
            idlingResource.decrement()
        }
    }

    fun update() {
        idlingResource.increment()
        GlobalScope.launch(Dispatchers.IO) {
            fillShopData()
            var ords: Result<List<UserOrderData>>?
            if (loggedUserInfo.role?.mapClient == true) {
                ords = shopRepository.getOrders()
            } else {
                ords = shopRepository.getCourierAvailableOrders()
            }
            if (ords is Result.Success) {

                ords.data.forEach {
                    val shopItems = shopData.value
                    it.items.forEach { oil ->
                        val shopItem = shopItems?.firstOrNull { it.id == oil.itemId }
                        oil.name = shopItem?.name
                        oil.photo = shopItem?.photo
                    }
                }
                orders.postValue(ords.data)
            }
            idlingResource.decrement()
        }
    }

    fun setStatus(order: Int, status: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            shopRepository.setStatus(order, status)
        }
    }

}

