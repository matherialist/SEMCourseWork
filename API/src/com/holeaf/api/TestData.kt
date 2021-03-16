package com.holeaf.api

import PlaceInfo
import UserInformation
import com.holeaf.api.API.*
import com.holeaf.api.model.ItemData
import com.holeaf.api.model.Role
import com.holeaf.api.model.SupplyContractData
import com.holeaf.api.model.TelemetryData
import java.util.*

val AdminRole = Role(
    id = 2,
    name = "admin",
    chatEveryone = true,
    chatCourier = true,
    chatSupplier = true,
    chatManager = true,
    chatClient = true,
    chatSeller = true,
    mapClient = true,
    mapSeller = true,
    mapManager = true,
    mapCourier = true,
    storeClient = true,
    storeSeller = true,
    supplies = true,
    orders = true,
    telemetry = true,
    stats = true,
    keys = true,
    users = true
)

val SellerRole = Role(
    id = 4,
    name = "seller",
    chatEveryone = false,
    chatCourier = true,
    chatSupplier = true,
    chatManager = false,
    chatClient = false,
    chatSeller = false,
    mapClient = false,
    mapSeller = true,
    mapManager = false,
    mapCourier = false,
    storeClient = false,
    storeSeller = true,
    supplies = true,
    orders = false,
    telemetry = false,
    stats = true,
    keys = false,
    users = false
)

val SupplierRole = Role(
    id = 5,
    name = "supplier",
    chatEveryone = false,
    chatCourier = false,
    chatSupplier = false,
    chatManager = false,
    chatClient = false,
    chatSeller = true,
    mapClient = false,
    mapSeller = false,
    mapManager = false,
    mapCourier = false,
    storeClient = false,
    storeSeller = false,
    supplies = true,
    orders = false,
    telemetry = false,
    stats = false,
    keys = false,
    users = false
)

val RegulatorRole = Role(
    id = 6,
    name = "regulator",
    chatEveryone = false,
    chatCourier = false,
    chatSupplier = false,
    chatManager = false,
    chatClient = false,
    chatSeller = false,
    mapClient = false,
    mapSeller = false,
    mapManager = false,
    mapCourier = false,
    storeClient = false,
    storeSeller = false,
    supplies = false,
    orders = false,
    telemetry = true,
    stats = true,
    keys = true,
    users = false
)

val CourierRole = Role(
    id = 3,
    name = "courier",
    chatEveryone = false,
    chatCourier = false,
    chatSupplier = false,
    chatManager = false,
    chatClient = false,
    chatSeller = true,
    mapClient = false,
    mapSeller = false,
    mapManager = false,
    mapCourier = true,
    storeClient = true,
    storeSeller = false,
    supplies = false,
    orders = true,
    telemetry = false,
    stats = false,
    keys = false,
    users = false
)

val ManagerRole = Role(
    id = 7,
    name = "manager",
    chatEveryone = false,
    chatCourier = true,
    chatSupplier = false,
    chatManager = true,
    chatClient = false,
    chatSeller = false,
    mapClient = false,
    mapSeller = false,
    mapManager = true,
    mapCourier = false,
    storeClient = false,
    storeSeller = false,
    supplies = false,
    orders = false,
    telemetry = false,
    stats = false,
    keys = false,
    users = false
)

val ClientRole = Role(
    id = 0,
    name = "client",
    chatEveryone = false,
    chatCourier = false,
    chatSupplier = false,
    chatManager = false,
    chatClient = false,
    chatSeller = false,
    mapClient = true,
    mapSeller = false,
    mapManager = false,
    mapCourier = false,
    storeClient = true,
    storeSeller = false,
    supplies = false,
    orders = true,
    telemetry = false,
    stats = false,
    keys = false,
    users = false
)


data class OrderDataTest(
    val orderId: Int,
    val clientId: Int,
    val courierId: Int,
    val totalCost: Int,
    val startDate: String,
    var closeDate: String,
    val long: Float,
    val lat: Float,
    var status: Int,
    val orderItemList: List<OrderItemList>
)

object TestData {
    val users = mutableListOf(
        UserInformation(1, "admin", "admin@test.com", AdminRole.id),
        UserInformation(2, "seller", "seller@test.com", SellerRole.id),
        UserInformation(3, "supplier", "supplier@test.com", SupplierRole.id),
        UserInformation(4, "regulator", "regulator@test.com", RegulatorRole.id),
        UserInformation(5, "courier", "courier@test.com", CourierRole.id),
        UserInformation(6, "manager", "manager@test.com", ManagerRole.id),
        UserInformation(7, "client", "client@test.com", ClientRole.id),
        UserInformation(52, "courier2", "courier2@test.com", CourierRole.id),
    )

    var cart = mutableMapOf(
        "admin" to mutableListOf(CartItemData(1, 1)),
        "client" to mutableListOf(CartItemData(1, 1))
    )

    var store = mutableListOf(
        ItemData(
            1, "Super Green", 500, 100, "Good shit",
            Base64.getEncoder().encodeToString(byteArrayOf())
        ),
        ItemData(
            2, "Simple Green", 400, 100, "Good shit",
            Base64.getEncoder().encodeToString(byteArrayOf())
        ),
        ItemData(
            3, "Sweet Yellow", 500, 100, "Good shit",
            Base64.getEncoder().encodeToString(byteArrayOf())
        ),
        ItemData(
            4, "Gold Standard", 600, 100, "Good shit",
            Base64.getEncoder().encodeToString(byteArrayOf())
        ),
        ItemData(
            5, "Holy Water", 1000, 100, "Good shit",
            Base64.getEncoder().encodeToString(byteArrayOf())
        )
    )

    var districts = mutableListOf(
        DistrictData(
            1, "District1", 0F, 0F, 1F, 1F,
            mutableListOf(1, 2)
        ),
        DistrictData(
            2, "District2", 1F, 1F, 2F, 2F,
            mutableListOf(3)
        )
    )

    var places = mutableListOf(PlaceInfo(1, 7, 0F, 0F))

    var supplyContracts = mutableListOf(
        SupplyContractData(1, 1000, "2020-12-25", "2020-12-31", 0),
        SupplyContractData(2, 2000, "2020-12-25", "2020-12-31", 0)
    )

    var contractLists = mutableListOf(
        SupplyList(1, 10, 100, 1),
        SupplyList(2, 15, 400, 1)
    )

    var telemetry = mutableListOf(TelemetryData(1, ""), TelemetryData(2, ""))

    var orders = mutableListOf(OrderDataTest(orderId = 1, clientId = 7, courierId = 5,
        totalCost = 1000, startDate = "2020-12-25", closeDate = "2020-12-31", long = 0F, lat = 0F, status = 1,
        orderItemList = mutableListOf(OrderItemList(1,2,500))
    ))

}