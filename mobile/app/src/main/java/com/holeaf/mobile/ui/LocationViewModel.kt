package com.holeaf.mobile.ui

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.holeaf.mobile.data.chat.ShopRepository
import com.holeaf.mobile.data.login.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

data class CourierInfo(val name: String, val photo: String)

data class GeoPosition(val id: Int, val latitude: Float, val longitude: Float)

class LocationViewModel : ViewModel(), KoinComponent {
    val repository: ShopRepository = get()

    val username: String = ""

    var location: MutableLiveData<Location?> = MutableLiveData(null)

    var courierWaiting: MutableLiveData<Int?> =
        MutableLiveData(null)       // null - нет ожидания, 0 - ожидание назначения, число - ID курьера

    var positions: MutableLiveData<List<GeoPosition>> = MutableLiveData()

    var courier: MutableLiveData<CourierInfo> = MutableLiveData()

    fun getCourierForActiveOrder() {
        GlobalScope.launch(Dispatchers.IO) {
            val orders = repository.getOrders()
            if (orders is Result.Success) {
                val order = orders.data.filter { it.status == 1 }.firstOrNull()
                if (order != null) {
                    //найден заказ - получаем фото и имя курьера
                    courier.postValue(CourierInfo(order.courierName, order.courierPhoto))
                }
            }
        }
    }
}
