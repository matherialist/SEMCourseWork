package com.holeaf.mobile.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.holeaf.mobile.data.login.Result
import com.holeaf.mobile.data.login.model.LoggedUserInfo
import com.holeaf.mobile.data.maps.MapsService
import com.holeaf.mobile.data.orders.models.UserOrderData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class TelemetryPosition(val lat: Float, val long: Float)

class MapsViewModel : ViewModel(), KoinComponent {
    val telemetry: MutableLiveData<Map<Int, List<LatLng>>> = MutableLiveData()
    val currentOrders: MutableLiveData<List<UserOrderData>> = MutableLiveData()
    val loggedUserInfo: LoggedUserInfo by inject()

    val mapsService: MapsService by inject()

    fun update() {
        if (loggedUserInfo.role?.mapCourier == true) {
            GlobalScope.launch(Dispatchers.IO) {
                currentOrders.postValue(mapsService.getCurrentOrders())
                val ids = mapsService.getTelemetryList()
                if (ids is Result.Success) {
                    var result = mutableMapOf<Int, List<LatLng>>()
                    ids.data.forEach {
                        val telemetry = mapsService.getTelemetry(it)
                        if (telemetry is Result.Success) {
                            result[it] = telemetry.data.map {
                                LatLng(it.lat.toDouble(), it.long.toDouble())
                            }
                        }
                    }
                    telemetry.postValue(result)
                }
            }
        } else {
            currentOrders.postValue(listOf())
        }
    }
}

