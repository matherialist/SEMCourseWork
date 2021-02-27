package com.holeaf.api.API

import com.holeaf.api.*
import com.holeaf.api.model.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.SimpleDateFormat
import java.util.*


@Location("/order")
class Order

@Location("/order/{orderId}")
class OrderId(val orderId: Int)

@Location("/order/{orderId}/{newStatus}")
class OrderStatus(val orderId: Int, val newStatus: Int)

@Location("/order/district/")
class OrderDistrict

fun Route.orders(
    orderManagementInterface: OrderManagementInterface,
    cartManagementInterface: CartManagementInterface
) {

    // Получить информацию о всех заказах пользователя
    get<Order> {
        holeafApplication.validationInterface.checkAccess(call, { it?.orders == true }) {
            val principal = call.principal<UserIdPrincipal>()!!
            launch {
                call.respond(HttpStatusCode.OK, orderManagementInterface.getOrders(principal.name))
            }
        }
    }

    // Получить перечень заказов для района/курьера
    get<OrderDistrict> {
        holeafApplication.validationInterface.checkAccess(call, { it?.orders == true }) {
            val principal = call.principal<UserIdPrincipal>()!!
            val orders = orderManagementInterface.getOrdersForDistrict(principal.name)
            if (orders.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, orders)
            } else {
                call.respond(HttpStatusCode.OK, listOf<UserOrderData>())
            }
        }
    }

    // Создать заказ
    post<Order> {
        holeafApplication.validationInterface.checkAccess(call, { it?.orders == true }) {
            val principal = call.principal<UserIdPrincipal>()!!
            val place = call.receive<PlaceData>()
            if (orderManagementInterface.createOrder(principal.name, place)) {
                launch {
                    cartManagementInterface.clearCart(principal.name)
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Заказ добавлен"))
                }
            } else {
                launch {
                    call.respond(HttpStatusCode.InternalServerError, "Ошибка при добавлении заказа")
                }
            }
        }
    }

    // Изменить статус заказа
    post<OrderStatus> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.orders == true || it?.storeClient == true }) {
            val principal = call.principal<UserIdPrincipal>()!!
            val orderId = data.orderId
            val newStatus = data.newStatus
            if (!orderManagementInterface.searchOrder(orderId)) {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Заказ не найден"))
            } else {
                if (orderManagementInterface.setStatus(principal.name, orderId, newStatus)) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Статус заказа изменён"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Неверный статус заказа"))
                }
            }
        }
    }

    // Получить информацию о заказе
    get<OrderId> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.orders == true }) {
            val orderId = data.orderId
            if (orderManagementInterface.searchOrder(orderId)) {
                call.respond(HttpStatusCode.OK, orderManagementInterface.getOrderDetails(orderId))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Заказ не найден"))
            }
        }
    }
}


interface OrderManagementInterface {
    //поиск заказа (проверка наличия)
    fun searchOrder(orderId: Int): Boolean

    //проверка наличия товара в заказе
    fun searchItem(orderId: Int, itemId: Int): Boolean

    //получение всех заказов пользователя
    fun getOrders(userName: String): List<UserOrderData>

    //получение всех заказов в районе
    fun getOrdersForDistrict(courierName: String): List<UserOrderData>

    //создание нового заказа
    fun createOrder(userName: String, place: PlaceData): Boolean

    //установка статуса заказа
    fun setStatus(userName: String, orderId: Int, newStatus: Int): Boolean

    // Получить статус заказа
    fun getStatus(orderId: Int): Int

    //получение деталей заказа
    fun getOrderDetails(orderId: Int): OrderData
}

data class PlaceData(val lat: Float, val long: Float)
data class DistrictBounds(val latTop: Float, val longTop: Float, val latBot: Float, val longBot: Float)

data class OrderItemList(val itemId: Int, val amount: Int, val price: Int)
data class OrderData(
    val orderId: Int,
    val clientId: Int,
    val totalCost: Int,
    val startDate: String,
    val closeDate: String,
    val long: Float,
    val lat: Float,
    var status: Int,
    val orderItemList: List<OrderItemList>
)

data class OrderInfo(val orderId: Int, val openDate: String)

class OrderManagement : OrderManagementInterface {
    override fun searchOrder(orderId: Int): Boolean {
        return transaction {
            val foundOrder = Orders.select { Orders.id eq orderId }
            return@transaction foundOrder.count() > 0
        }
    }

    override fun searchItem(orderId: Int, itemId: Int): Boolean {
        return transaction {
            val foundItem = OrderLists.select {
                (OrderLists.orderId eq orderId) and (OrderLists.itemId eq itemId)
            }
            return@transaction foundItem.count() > 0
        }
    }

    fun getItems(orderId: Int):Pair<List<OrderItemList>, Int> {
        val orderItemList = OrderLists.select { OrderLists.orderId eq orderId }
            .map {
                OrderItemList(
                    itemId = it[OrderLists.itemId].value,
                    amount = it[OrderLists.amount],
                    price = it[OrderLists.price]
                )
            }
        var cost = 0
        orderItemList.forEach() {
            cost += it.amount * it.price
        }
        return orderItemList to cost
    }

    override fun getOrders(userName: String): List<UserOrderData> {
        return transaction {
            val userId = Users.select { Users.username eq userName }.single()[Users.id]
            Orders.select { Orders.client eq userId }
                .map {
                    val courier = it[Orders.courier]?.value
                    val courierInfo = Users.select { Users.id eq courier }.singleOrNull()
                    val photo = courierInfo?.get(Users.photo)
                    var courierPhoto = ""
                    photo?.let {
                        courierPhoto = Base64.getEncoder().encodeToString(it.bytes)
                    }
                    var courierName = "?"
                    courierInfo?.let {
                        courierName = it[Users.username]
                    }
                    val data = getItems(it[Orders.id].value)

                    UserOrderData(
                        id = it[Orders.id].value,
                        totalCost = data.second,
                        openDate = it[Orders.openTime],
                        closeDate = it[Orders.closeTime],
                        status = it[Orders.status],
                        lat = it[Orders.latitude],
                        long = it[Orders.longitude],
                        courierName = courierName,
                        courierPhoto = courierPhoto,
                        items = data.first
                    )
                }.sortedBy { -it.id }
        }
    }

    override fun getOrdersForDistrict(courierName: String): List<UserOrderData> {
        return transaction {
            val courierId = Users.select { Users.username eq courierName }.single()[Users.id].value
            val districts = DistrictCouriers.select {
                DistrictCouriers.courier eq courierId
            }.map { it[DistrictCouriers.district].value }
            var returnOrders = mutableListOf<UserOrderData>()
            districts.forEach { district ->

                val bounds_query= Districts.select { Districts.id eq district }.firstOrNull()
                val bounds = bounds_query?.let {
                    DistrictBounds(
                        longTop = it[Districts.longitudeTop],
                        latTop = it[Districts.latitudeTop],
                        longBot = it[Districts.longitudeBot],
                        latBot = it[Districts.latitudeBot]
                    )
                }
                if (bounds != null) {
                    // ((x-xl)(x-xr)<0 && (y-yt)(y-yb)<0)
                    returnOrders.addAll(Orders.selectAll().filter {
                        val latitude = it[Orders.latitude]
                        val longitude = it[Orders.longitude]
                        val status = it[Orders.status]
                        val courier = it[Orders.courier]?.value
                        ((status == 0 && courier==null) || (status == 1) && (courier == courierId)) &&
                                ((latitude - bounds.latTop) * (latitude - bounds.latBot) < 0) &&
                                ((longitude - bounds.longTop) * (longitude - bounds.longBot) < 0)
                    }.map {
                        val data = getItems(it[Orders.id].value)
                        UserOrderData(
                            id = it[Orders.id].value,
                            totalCost = data.second,
                            openDate = it[Orders.openTime],
                            closeDate = it[Orders.closeTime],
                            status = it[Orders.status],
                            lat = it[Orders.latitude],
                            long = it[Orders.longitude],
                            courierName = "",
                            courierPhoto = "",
                            items = data.first
                        )
                    })
                }
            }
            returnOrders.sortedBy { -it.id }
        }
    }

    override fun createOrder(userName: String, place: PlaceData): Boolean {
        transaction {
            val userId = Users.select { Users.username eq userName }.single()[Users.id].value
            val startDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"))
            val isoDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val itemList = Carts.select { Carts.userId eq userId }
                .map { item ->
                    OrderItemList(
                        itemId = item[Carts.itemId].value,
                        amount = item[Carts.amount],
                        price = Goods.select { Goods.id eq item[Carts.itemId] }.single()[Goods.price]
                    )
                }
            var cost = 0
            itemList.forEach()
            {
                cost += it.amount * it.price
            }
            val orderId = Orders.insert {
                it[client] = userId
                it[totalCost] = cost
                it[openTime] = isoDateFormat.format(startDate.time)
                it[closeTime] = isoDateFormat.format(startDate.time)
                it[longitude] = place.long
                it[latitude] = place.lat
                it[status] = 0
            } get (Orders.id)

            itemList.forEach { item ->
                OrderLists.insert {
                    it[OrderLists.orderId] = orderId
                    it[itemId] = item.itemId
                    it[amount] = item.amount
                    it[price] = item.price
                }
            }
        }
        return true
    }

    override fun setStatus(userName: String, orderId: Int, newStatus: Int): Boolean {
        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"))
        return if (newStatus == 1) {
            transaction {
                val courierId = Users.select { Users.username eq userName }.single()[Users.id]
                Orders.update({ Orders.id eq orderId }) {
                    it[status] = 1
                    it[courier] = courierId
                }
            }
            true
        } else {
            if (newStatus == 2 || newStatus == 3) {
                transaction {
                    Orders.update({ Orders.id eq orderId }) {
                        it[status] = newStatus
                        it[closeTime] = isoDateFormat.format(currentDate.time)
                    }
                }
                true
            } else {
                false
            }
        }
    }

    override fun getStatus(orderId: Int): Int {
        return Orders.select { Orders.id eq orderId }.single()[Orders.id].value
    }

    override fun getOrderDetails(orderId: Int): OrderData {
        return transaction {
            val order = Orders.select { Orders.id eq orderId }.single()
            val orderItemList = OrderLists.select { OrderLists.orderId eq orderId }
                .map {
                    OrderItemList(
                        itemId = it[OrderLists.itemId].value,
                        amount = it[OrderLists.amount],
                        price = it[OrderLists.price]
                    )
                }
            var cost = 0
            orderItemList.forEach()
            {
                cost += it.amount * it.price
            }
            OrderData(
                orderId = orderId,
                clientId = order[Orders.client].value,
                totalCost = cost,
                startDate = order[Orders.openTime],
                closeDate = order[Orders.closeTime],
                long = order[Orders.longitude],
                lat = order[Orders.latitude],
                status = order[Orders.status],
                orderItemList = orderItemList
            )
        }
    }
}


class OrderManagementFake : OrderManagementInterface {
    override fun searchOrder(orderId: Int): Boolean {
        return TestData.orders.firstOrNull { it.orderId == orderId } != null
    }

    override fun searchItem(orderId: Int, itemId: Int): Boolean {
        return TestData.orders.first { it.orderId == orderId }.orderItemList.firstOrNull { it.itemId == itemId } != null
    }

    fun getItems(orderId:Int):Pair<List<OrderItemList>, Int> {
        val items = TestData.orders.firstOrNull { it.orderId == orderId }?.orderItemList.orEmpty()
        val cost = items.sumBy { it.amount * it.price }
        return items to cost
    }

    override fun getOrders(userName: String): List<UserOrderData> {
        val userId = TestData.users.first { it.username == userName }.id
        return TestData.orders.filter { it.clientId == userId }.map { item ->
            val data = getItems(item.orderId)
            UserOrderData(
                id = item.orderId,
                totalCost = data.second,
                openDate = item.startDate,
                closeDate = item.closeDate,
                status = item.status,
                lat = item.lat,
                long = item.long,
                courierName = TestData.users.first {
                    TestData.orders.first {
                        it.orderId == item.orderId
                    }.courierId == item.courierId
                }.username,
                courierPhoto = "",
                items = data.first
            )
        }
    }

    override fun getOrdersForDistrict(courierName: String): List<UserOrderData> {
        val courierId = TestData.users.first { it.username == courierName }.id
        return TestData.orders.filter { it.courierId == courierId }.map {
            val data = getItems(it.orderId)
            UserOrderData(
                id = it.orderId,
                totalCost = data.second,
                openDate = it.startDate,
                closeDate = it.closeDate,
                status = 0,
                lat = it.lat,
                long = it.long,
                courierName = "",
                courierPhoto = "",
                items = data.first
            )
        }
    }

    override fun createOrder(userName: String, place: PlaceData): Boolean {
        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"))
        val userId = TestData.users.first { it.username == userName }.id
        val maxOrderId = TestData.orders.maxOf { it.orderId }
        val cart = TestData.cart[userName]?.map{ item ->
            OrderItemList(
                itemId = item.itemId,
                amount = item.amount,
                price = TestData.store.first { it.id ==  item.itemId}.price
            )
        }
        TestData.orders.add(
            OrderDataTest(
                maxOrderId + 1, userId, 0, 0, isoDateFormat.format(currentDate.time),
                "", place.long, place.lat, 0, cart?: mutableListOf()
            )
        )
        return true
    }

    override fun setStatus(userName: String, orderId: Int, newStatus: Int): Boolean {
        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val currentDate = Calendar.getInstance(TimeZone.getTimeZone("GMT+3"))
        return if (newStatus == 1) {
            TestData.orders.first { it.orderId == orderId }.status = newStatus
            true
        } else if (newStatus == 2 || newStatus == 3) {
            TestData.orders.first { it.orderId == orderId }.status = newStatus
            TestData.orders.first { it.orderId == orderId }.closeDate = isoDateFormat.format(currentDate.time)
            true
        } else {
            false
        }
    }

    override fun getStatus(orderId: Int): Int {
        return TestData.orders.first { it.orderId == orderId }.status
    }

    override fun getOrderDetails(orderId: Int): OrderData {
        return TestData.orders.filter { it.orderId == orderId }.map {
            OrderData(
                orderId = it.orderId,
                clientId = it.clientId,
                totalCost = it.totalCost,
                startDate = it.startDate,
                closeDate = it.closeDate,
                long = it.long,
                lat = it.lat,
                status = it.status,
                orderItemList = it.orderItemList
            )
        }.single()
    }
}