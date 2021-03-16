package com.holeaf.mobile.data.maps

import com.holeaf.mobile.MainApplication
import com.holeaf.mobile.data.login.Result
import com.holeaf.mobile.data.orders.models.UserOrderData
import com.holeaf.mobile.ui.TelemetryPosition

class MapsService {
    suspend fun getTelemetryList(): Result<List<Int>> {
        val sAuth = MainApplication.getServiceAuth()
        try {
            val ids = sAuth.getTelemetryList()
            return Result.Success(ids)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun getTelemetry(id: Int): Result<List<TelemetryPosition>> {
        val sAuth = MainApplication.getServiceAuth()
        try {
            val data = sAuth.getTelemetry(id)
            return Result.Success(data)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    suspend fun getCurrentOrders(): List<UserOrderData> {
        val sAuth = MainApplication.getServiceAuth()
        val data = sAuth.getCourierAvailableOrders()
        return data.filter { it.status == 1 }
    }
}