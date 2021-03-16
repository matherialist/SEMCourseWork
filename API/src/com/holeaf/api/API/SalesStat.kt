package com.holeaf.api.API

import com.holeaf.api.*
import com.holeaf.api.model.DailySaleStats
import com.holeaf.api.model.UserOrderData
import com.holeaf.api.model.OrderListData
import com.holeaf.api.model.ProductSaleStats
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.SimpleDateFormat
import java.util.*


@Location("/dailystat/{from}/{to}")
data class DailyStat(val from: String, val to: String)

@Location("/productstat/{from}/{to}")
data class ProductStat(val from: String, val to: String)


interface SaleStatsManagementInterface {
    fun getDailySaleStat(from: String, to: String): List<DailySaleStats>
    fun getProductSaleStat(from: String, to: String): List<ProductSaleStats>
}


fun Route.salestats(saleStatsManagementInterface: SaleStatsManagementInterface) {
    get<DailyStat> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.stats == true }) {
            launch {
                call.respond(HttpStatusCode.OK, saleStatsManagementInterface.getDailySaleStat(data.from, data.to))
            }
        }
    }

    get<ProductStat> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.stats == true }) {
            launch {
                call.respond(HttpStatusCode.OK, saleStatsManagementInterface.getProductSaleStat(data.from, data.to))
            }
        }
    }
}

class SaleStatsManagement : SaleStatsManagementInterface {
    override fun getDailySaleStat(from: String, to: String): List<DailySaleStats> {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd")
        val humanFormat = SimpleDateFormat("dd.MM.yyyy")
        val startDate = isoFormat.parse(from)
        val endDate = isoFormat.parse(to)
        var currentDate = startDate

        val result = mutableListOf<DailySaleStats>()

        //prepare empty result
        while (currentDate.before(endDate) || currentDate.equals(endDate)) {
            val prDate = isoFormat.format(currentDate)
            //select order with matching closeDate
            transaction {
                val orders = Orders.select {
                    Orders.closeTime like "${prDate}%"
                }
                var sum = 0
                orders.forEach { order ->
                    sum += OrderLists.select {
                        OrderLists.orderId eq order[Orders.id].value
                    }.map { it[OrderLists.amount] * it[OrderLists.price] }.sum()
                }
                result.add(DailySaleStats(sum, humanFormat.format(currentDate)))
            }
            val cal = Calendar.getInstance()
            cal.time = currentDate
            cal.add(Calendar.DAY_OF_YEAR, 1)
            currentDate = cal.time
            print(currentDate)
        }
        return result
    }

    override fun getProductSaleStat(from: String, to: String): List<ProductSaleStats> {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd")
        val startDate = isoFormat.parse(from)
        val endDate = isoFormat.parse(to)
        var currentDate = startDate

        val result = mutableListOf<ProductSaleStats>()

        //prepare empty result
        transaction {
            Goods.selectAll().forEach {
                val id = it[Goods.id].value
                val found = result.firstOrNull { it.itemId == id }
                if (found == null) {
                    result.add(ProductSaleStats(id, it[Goods.title], 0, 0))
                }
            }
        }

        while (currentDate.before(endDate) || currentDate.equals(endDate)) {
            val prDate = isoFormat.format(currentDate)
            //select order with matching closeDate
            transaction {
                val orders = Orders.select {
                    Orders.closeTime like "${prDate}%"
                }
                orders.forEach { order ->
                    val items = OrderLists.select {
                        OrderLists.orderId eq order[Orders.id].value
                    }
                    items.forEach {
                        val id = it[OrderLists.itemId].value
                        val amount = it[OrderLists.amount]
                        val cost = amount * it[OrderLists.price]
                        result.firstOrNull { it.itemId == id }?.let {
                            it.amount += amount
                            it.totalCost += cost
                        }
                    }
                }
            }
            //...
            val cal = Calendar.getInstance()
            cal.time = currentDate
            cal.add(Calendar.DAY_OF_YEAR, 1)
            currentDate = cal.time
        }
        return result.filter { it.amount > 0 }
    }
}

class SaleStatsManagementFake : SaleStatsManagementInterface {
    private var orders = mutableListOf(UserOrderData(1, 1000, "2020-12-25", "2020-12-25", 2, 0F,0F,"", "", listOf(OrderItemList(1, 2, 500))))
    private var orderLists = mutableListOf(OrderListData(1, 2, 500, 1))

    override fun getDailySaleStat(from: String, to: String): List<DailySaleStats> {
        val result = mutableListOf<DailySaleStats>()
        val selectedOrders = orders.filter { it.openDate >= from && it.closeDate <= to }
        selectedOrders.forEach {
            result.add(DailySaleStats(it.totalCost, it.closeDate))
        }
        return result
    }

    override fun getProductSaleStat(from: String, to: String): List<ProductSaleStats> {
        val result = mutableListOf<ProductSaleStats>()
        val selectedOrders = orders.filter { it.openDate >= from && it.closeDate <= to }
        val orderIds = mutableListOf<Int>()
        selectedOrders.forEach { orderIds.add(it.id) }
        orderLists.filter { orderIds.contains(it.orderId) }.forEach { item ->
            if (result.firstOrNull { it.itemId == item.itemId } != null) {
                result.find { it.itemId == item.itemId }!!.amount += item.amount
                result.find { it.itemId == item.itemId }!!.totalCost += item.price * item.amount
            } else {
                result.add(
                    ProductSaleStats(
                        item.itemId,
                        TestData.store.first { it.id == item.itemId }.title,
                        item.price * item.amount,
                        item.amount
                    )
                )
            }
        }
        print("\n"+result)
        return result
    }
}