package com.holeaf.api.API

import com.holeaf.api.Goods
import com.holeaf.api.holeafApplication
import com.holeaf.api.TestData
import com.holeaf.api.model.ItemData
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

@Location("/store")
class Store

@Location("/store/{id}")
class StoreItem(val id: Int)


fun Route.store(storeManagementInterface: StoreManagementInterface) {
    get<Store> {
        holeafApplication.validationInterface.checkAccess(call, { it?.storeClient == true || it?.storeSeller == true }) {
            call.respond(storeManagementInterface.getGoodsList())
        }
    }

    get<StoreItem> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.storeClient == true || it?.storeSeller == true }) {
            val item = storeManagementInterface.getItem(data.id)
            if (item != null) {
                call.respond(HttpStatusCode.OK, item)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Товар не найден"))
            }
        }
    }

    post<Store> {
        print("Save to store\n")
        holeafApplication.validationInterface.checkAccess(call, { it?.storeSeller == true }) {
            val data = call.receive<ItemData>()
            print("\n$data\n")
            if (data.title.isBlank() || (data.price < 0) || (data.amount < 0)) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val dc = storeManagementInterface.addItem(data)
                launch {
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to if (dc) "Информация о товаре была успешно добавлена" else "Информация о товаре успешно обновлена")
                    )
                }
            }
        }
    }


    delete<StoreItem> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.storeSeller == true }) {
            val id = data.id
            if (storeManagementInterface.deleteItem(id)) {
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
}


interface StoreManagementInterface {
    fun getItem(id: Int): ItemData?
    fun getGoodsList(): List<ItemData>
    fun deleteItem(id: Int): Boolean
    fun addItem(data: ItemData): Boolean
}


class StoreManagement : StoreManagementInterface {
    override fun getItem(id: Int): ItemData? {
        var result: ItemData? = null
        transaction {
            val items = Goods.select { Goods.id eq id }
            if (items.count() > 0) {
                val item = items.single()
                val photo = item[Goods.photo]
                result = ItemData(
                    item[Goods.id].value, item[Goods.title], item[Goods.price], item[Goods.amount],
                    item[Goods.description],
                    Base64.getEncoder().encodeToString(photo?.bytes ?: byteArrayOf())
                )
            }
        }
        return result
    }

    override fun getGoodsList(): List<ItemData> {
        var result: List<ItemData> = mutableListOf()
        transaction {
            result = Goods.selectAll()
                .map {
                    val photo = it[Goods.photo]
                    ItemData(
                        id = it[Goods.id].value,
                        title = it[Goods.title],
                        price = it[Goods.price],
                        amount = it[Goods.amount],
                        description = it[Goods.description],
                        photo = Base64.getEncoder().encodeToString(photo?.bytes ?: byteArrayOf())
                    )
                }
        }
        return result
    }

    override fun deleteItem(id: Int): Boolean {
        if (getItem(id) != null) {
            transaction {
                Goods.deleteWhere { Goods.id eq id }
            }
            return true
        } else {
            return false
        }
    }

    override fun addItem(data: ItemData): Boolean {
        if (getItem(data.id) != null) {
            transaction {
                Goods.update({ Goods.id eq data.id }) {
                    it[amount] = data.amount
                    it[title] = data.title
                    it[price] = data.price
                    it[description] = data.description
                    it[photo] = ExposedBlob(Base64.getDecoder().decode(data.photo))
                }
            }
            return false
        } else {
            transaction {
                Goods.insert {
                    it[title] = data.title
                    it[price] = data.price
                    it[amount] = data.amount
                    it[description] = data.description
                    it[photo] = ExposedBlob(Base64.getDecoder().decode(data.photo))
                }
            }
            return true
        }
    }
}


class StoreManagementFake : StoreManagementInterface {
    override fun getItem(id: Int): ItemData? {
        return TestData.store.firstOrNull { it.id == id }
    }

    override fun getGoodsList(): List<ItemData> {
        return TestData.store
    }

    override fun addItem(data: ItemData): Boolean {
        return if (data.price >= 0 && data.amount >= 0) {
            if (getItem(data.id) != null) {
                TestData.store.find { it.id == data.id }!!.price = data.price
                TestData.store.find { it.id == data.id }!!.amount += data.amount
                TestData.store.find { it.id == data.id }!!.title = data.title
                TestData.store.find { it.id == data.id }!!.description = data.description
                true
            } else {
                TestData.store.add(data)
                true
            }
        } else {
            false
        }
    }

    override fun deleteItem(id: Int): Boolean {
        val len = TestData.store.count()
        TestData.store.removeIf { it.id == id }
        return len > TestData.store.count()
    }
}