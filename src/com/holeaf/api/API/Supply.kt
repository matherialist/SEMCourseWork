package com.holeaf.api.API


import com.holeaf.api.*
import com.holeaf.api.model.*
import io.ktor.application.*
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

@Location("/supply")
class Supply

@Location("/supply/{id}")
class SupplyContract(val id: Int)

@Location("/supply/{id}/{item}")
class SupplyItem(val id: Int, val item: Int)

@Location("/supply/status/{id}/{status}")
class SupplyStatusUpdate(val id: Int, val status: Int)

data class SupplyContractItemData(val itemId: Int, val amount: Int, val price: Int)

fun Route.supplies(
    supplyManagementInterface: SupplyManagementInterface,
    storeManagementInterface: StoreManagementInterface
) {
    // Получить информацию о всех договорах на поставку
    get<Supply> {
        holeafApplication.validationInterface.checkAccess(call, { it?.supplies == true }) {
            launch {
                call.respond(supplyManagementInterface.getContracts())
            }
        }
    }

    // Создать пустой договор на поставку
    post<Supply> {
        holeafApplication.validationInterface.checkAccess(call, { it?.supplies == true }) {
            supplyManagementInterface.createContract(0, listOf())
            call.respond(HttpStatusCode.OK, mapOf("message" to "Контракт на поставку добавлен"))
        }
    }

    // Удалить договор
    delete<SupplyContract> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.supplies == true }) {
            val contractId = data.id
            if (contractId < 0) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                if (supplyManagementInterface.searchContract(contractId)) {
                    supplyManagementInterface.deleteContract(contractId)
                    launch {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Контракт удалён"))
                    }
                } else {
                    launch {
                        call.respond(HttpStatusCode.NotFound, mapOf("message" to "Контракт не найден"))
                    }
                }
            }
        }
    }

    // Получить перечень товаров для заданного договора
    get<SupplyContract> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.supplies == true }) {
            val contractId = data.id
            if (supplyManagementInterface.searchContract(contractId)) {
                call.respond(HttpStatusCode.OK, supplyManagementInterface.getContractDetails(contractId))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Договор не найден"))
            }
        }
    }

    // Добавить товар в список поставки
    post<SupplyItem> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.supplies == true }) {
            val contractId = data.id
            val data = call.receive<SupplyContractItemData>()
            print("\nProcessing data: $data\n")

            if ((data.amount < 0) || (data.price < 0)) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                if (!supplyManagementInterface.searchContract(contractId)) {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Договор не найден"))
                } else {
                    supplyManagementInterface.addItemToContract(contractId, data)
                    launch {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Товар добавлен в договор поставки"))
                    }
                }
            }
        }
    }

    // Удалить товар из списка поставки
    delete<SupplyItem> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.supplies == true }) {
            if (supplyManagementInterface.searchItem(data.id, data.item)) {
                supplyManagementInterface.deleteItemFromContract(data.id, data.item)
                launch {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Товар удалён"))
                }
            } else {
                launch {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Товар не найден"))
                }
            }
        }
    }

    get<SupplyStatusUpdate> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.supplies == true }) {
            if (supplyManagementInterface.searchContract(data.id)) {
                supplyManagementInterface.setStatus(data.id, data.status)
                if (data.status == 2) {
                    //update store
                    val items = supplyManagementInterface.getContractDetails(data.id)
                    items.forEach { item ->
                        val storeState = storeManagementInterface.getItem(item.itemId)
                        storeState?.let {
                            storeState.amount += item.amount
                            storeManagementInterface.addItem(storeState)
                        }
                    }
                }
                call.respond(HttpStatusCode.OK, mapOf("message" to "Состояние изменено"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Договор не найден"))
            }
        }
    }

}


interface SupplyManagementInterface {
    //поиск договора (проверка наличия)
    fun searchContract(contractId: Int): Boolean

    //проверка наличия товара в договоре
    fun searchItem(contractId: Int, itemId: Int): Boolean

    //получение всех контрактов
    fun getContracts(): List<SupplyContractData>

    //установка статуса контракта
    fun setStatus(contractId: Int, status: Int): Boolean

    //создание нового контракта
    fun createContract(status: Int, supplyList: List<SupplyList>): Boolean

    //получение списка позиций контракта
    fun getContractDetails(contractId: Int): List<SupplyList>

    //добавить позицию в договор
    fun addItemToContract(contractId: Int, data: SupplyContractItemData): Boolean

    //удалить позицию из договора
    fun deleteItemFromContract(contractId: Int, itemId: Int): Boolean

    //удаление договора
    fun deleteContract(contractId: Int): Boolean
}

fun transformDate(date: String?): String {
    if (date.isNullOrEmpty()) return ""
    try {
        print("\nSource date is $date")
        val isoFormat = SimpleDateFormat("yyyy-MM-dd")
        val humanFormat = SimpleDateFormat("dd.MM.yyyy")
        print("\nParsed date is ${isoFormat.parse(date)}")
        return humanFormat.format(isoFormat.parse(date))
    } catch (e: Exception) {
        print("Excepted")
        return date
    }
}

data class SupplyList(val itemId: Int, var amount: Int, var price: Int, val contractId: Int)

fun recalculateSum(id: Int) {
    transaction {
        //recalculate order sum
        val products = SupplyLists.select {
            SupplyLists.contractId eq id
        }
        var sum = 0
        products.forEach {
            sum += it[SupplyLists.amount] * it[SupplyLists.price]
        }
        SupplyContracts.update({ SupplyContracts.id eq id }) {
            it[totalCost] = sum
        }
    }
}

class SupplyManagement : SupplyManagementInterface {
    override fun searchContract(contractId: Int): Boolean {
        return transaction {
            val foundContract = SupplyContracts.select { SupplyContracts.id eq contractId }
            return@transaction foundContract.count() > 0
        }
    }

    override fun searchItem(contractId: Int, itemId: Int): Boolean {
        return transaction {
            val foundItem = SupplyLists.select {
                (SupplyLists.contractId eq contractId) and (SupplyLists.itemId eq itemId)
            }
            return@transaction foundItem.count() > 0
        }
    }

    override fun getContracts(): List<SupplyContractData> {
        return transaction {
            SupplyContracts.selectAll().orderBy(SupplyContracts.id).map {
                SupplyContractData(
                    it[SupplyContracts.id].value,
                    it[SupplyContracts.totalCost],
                    transformDate(it[SupplyContracts.openDate]),
                    transformDate(it[SupplyContracts.closeDate]),
                    it[SupplyContracts.status]
                )
            }
        }
    }

    override fun setStatus(contractId: Int, newStatus: Int): Boolean {
        print("\nSet status: $contractId, $newStatus")
        val currentDate = Calendar.getInstance()
        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd")
        return if (searchContract(contractId)) {
            transaction {
                SupplyContracts.update({ SupplyContracts.id eq contractId }) {
                    it[status] = newStatus
                    if (newStatus == 2 || newStatus == 3) {
                        print("\nCloseDate: ${isoDateFormat.format(currentDate.time)}")
                        it[closeDate] = isoDateFormat.format(currentDate.time)
                    }
                }
            }
            true
        } else {
            false
        }
    }

    override fun createContract(status: Int, supplyList: List<SupplyList>): Boolean {
        val startDate = Calendar.getInstance()
        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd")
        var cost = 0
        supplyList.forEach {
            cost += it.amount * it.price
        }
        transaction {
            val newId = SupplyContracts.insert {
                it[totalCost] = cost
                it[openDate] = isoDateFormat.format(startDate.time)
            } get (SupplyContracts.id)
            supplyList.forEach { item ->
                SupplyLists.insert {
                    it[amount] = item.amount
                    it[contractId] = newId.value
                    it[itemId] = item.itemId
                    it[price] = item.price
                }
            }
        }
        return true
    }

    override fun getContractDetails(contractId: Int): List<SupplyList> {
        return transaction {
            SupplyLists.select { SupplyLists.contractId eq contractId }.orderBy(SupplyLists.id).map {
                SupplyList(
                    it[SupplyLists.itemId].value,
                    it[SupplyLists.amount],
                    it[SupplyLists.price],
                    it[SupplyLists.contractId].value
                )
            }
        }
    }

    override fun addItemToContract(contractId: Int, data: SupplyContractItemData): Boolean {
        return if (searchContract(contractId)) {
            if (searchItem(contractId, data.itemId)) {
                transaction {
                    SupplyLists.update({
                        (SupplyLists.contractId eq contractId) and (SupplyLists.itemId eq data.itemId)
                    }) {
                        with(SqlExpressionBuilder) {
                            it.update(amount, amount + data.amount)
                        }
                        it[price] = data.price
                    }
                }
            } else {
                transaction {
                    SupplyLists.insert {
                        it[this.contractId] = contractId
                        it[itemId] = data.itemId
                        it[price] = data.price
                        it[amount] = data.amount
                    }
                }
            }
            recalculateSum(contractId)
            true
        } else {
            false
        }
    }

    override fun deleteItemFromContract(contractId: Int, itemId: Int): Boolean {
        return if (searchContract(contractId) && searchItem(contractId, itemId)) {
            transaction {
                SupplyLists.deleteWhere {
                    (SupplyLists.contractId eq contractId) and
                            (SupplyLists.itemId eq itemId)
                }
            }
            recalculateSum(contractId)
            true
        } else {
            false
        }
    }

    override fun deleteContract(contractId: Int): Boolean {
        return if (searchContract(contractId)) {
            transaction {
                SupplyLists.deleteWhere { SupplyLists.contractId eq contractId }
                SupplyContracts.deleteWhere { SupplyContracts.id eq contractId }
                true
            }
        } else {
            false
        }
    }
}


class SupplyManagementFake : SupplyManagementInterface {
    override fun searchContract(contractId: Int): Boolean {
        return TestData.supplyContracts.firstOrNull { it.id == contractId } != null
    }

    override fun searchItem(contractId: Int, itemId: Int): Boolean {
        return TestData.contractLists.firstOrNull { it.contractId == contractId && it.itemId == itemId } != null
    }

    override fun getContracts(): List<SupplyContractData> {
        return TestData.supplyContracts
    }

    override fun setStatus(contractId: Int, status: Int): Boolean {
        return if (searchContract(contractId)) {
            TestData.supplyContracts.find { it.id == contractId }!!.status = status
            true
        } else {
            false
        }
    }

    override fun createContract(status: Int, supplyList: List<SupplyList>): Boolean {
        var cost = 0
        supplyList.forEach {
            cost += it.amount * it.price
        }
        TestData.supplyContracts.add(
            SupplyContractData(
                TestData.supplyContracts.maxOf { it.id } + 1, cost, "2020-12-25",
                "2020-12-31", status
            )
        )
        supplyList.forEach { TestData.contractLists.add(it) }
        return true
    }

    override fun getContractDetails(contractId: Int): List<SupplyList> {
        return TestData.contractLists.filter { it.contractId == contractId }
    }

    override fun addItemToContract(contractId: Int, data: SupplyContractItemData): Boolean {
        return if (searchItem(contractId, data.itemId)) {
            TestData.contractLists.first { (it.contractId == contractId) && (it.itemId == data.itemId) }.amount += data.amount
            TestData.contractLists.first { (it.contractId == contractId) && (it.itemId == data.itemId) }.price = data.price
            true
        } else {
            TestData.contractLists.add(SupplyList(data.itemId, data.amount, data.price, contractId))
            true
        }
    }

    override fun deleteItemFromContract(contractId: Int, itemId: Int): Boolean {
        return if (searchContract(contractId)) {
            if (searchItem(contractId, itemId)) {
                TestData.contractLists.removeIf { it.contractId == contractId && it.itemId == itemId }
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    override fun deleteContract(contractId: Int): Boolean {
        return if (searchContract(contractId)) {
            TestData.supplyContracts.removeIf { it.id == contractId }
            TestData.contractLists.removeIf { it.contractId == contractId }
            true
        } else {
            false
        }
    }
}