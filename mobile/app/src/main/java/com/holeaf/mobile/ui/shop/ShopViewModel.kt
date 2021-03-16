package com.holeaf.mobile.ui.shop

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import com.holeaf.mobile.data.chat.ShopRepository
import com.holeaf.mobile.data.login.Result
import com.holeaf.mobile.ui.LocationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


data class CartItem(val id: Int, var amount: Int)

class ShopViewModel(val shopRepository: ShopRepository) :
    ViewModel() {
    val idlingResource = CountingIdlingResource("CreateOrder")

    init {
        IdlingRegistry.getInstance().register(idlingResource)
    }

    var shopData = MutableLiveData(mutableListOf<ShopItemUi>())

    var cart = MutableLiveData(mutableListOf<CartItem>())

    var pollingTimer: Timer? = null
    var pollingTimerTask: TimerTask? = null

    fun update(locationViewModel: LocationViewModel) {
        idlingResource.increment()
        GlobalScope.launch(Dispatchers.IO) {
            val result = shopRepository.getShop()
            val cartItems = shopRepository.getCart()
            val newCartItems = mutableListOf<CartItem>()
            if (cartItems is Result.Success) {
                cartItems.data.forEach {
                    newCartItems.add(CartItem(it.itemId, it.amount))
                }
            }
            if (result is Result.Success) {
                shopData.postValue(result.data.map {
                    //todo: description
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
                    val inCart = newCartItems.firstOrNull { cartItem -> it.id == cartItem.id }
                    val amount = inCart?.amount ?: 0
                    ShopItemUi(it.id, it.title, it.description.orEmpty(), it.price, photo, amount)
                }.toMutableList())
            }
            cart.postValue(newCartItems)

            //получение списка заказов, поиск активных
            val orders = shopRepository.getOrders()
            val active = if (orders is Result.Success) {
                orders.data.firstOrNull { it.status == 0 } != null
            } else {
                false
            }
            if (active) {
                locationViewModel.courierWaiting.postValue(0)
            } else {
                locationViewModel.courierWaiting.postValue(null)
            }
            pollingTimer = Timer()
            pollingTimerTask = object : TimerTask() {
                override fun run() {
                    GlobalScope.launch(Dispatchers.IO) {
                        val orders = shopRepository.getOrders()
                        val active = if (orders is Result.Success) {
                            orders.data.firstOrNull { it.status == 0 } != null
                        } else {
                            false
                        }
                        if (!active && locationViewModel.courierWaiting.value == 0) {
                            locationViewModel.courierWaiting.postValue(1)
                            //переход на страницу с информацией о курьере при успешном назначении (если не пришел сигнал через web socket)
                        }
                    }
                }
            }
            pollingTimer?.schedule(
                pollingTimerTask!!,
                30 * 1000,
                30 * 1000
            )        //опрос каждые 30 секунд

            idlingResource.decrement()
        }
    }

}

