package com.holeaf.api.API

import com.holeaf.api.*
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

@Location("/cart")
class Cart

@Location("/cart/{id}")
class CartItemLocation(val id: Int)

data class CartItemData(val itemId: Int, val amount: Int)

fun Route.carts(cartManagementInterface: CartManagementInterface) {
    // Получить перечень товаров и их количество
    get<Cart> {
        holeafApplication.validationInterface.checkAccess(call, { it?.storeClient == true }) {
            val principal = call.principal<UserIdPrincipal>()!!
            launch {
                call.respond(HttpStatusCode.OK, cartManagementInterface.getCartItems(principal.name))
            }
        }
    }

    // Добавить товар в корзину
    post<Cart> {
        holeafApplication.validationInterface.checkAccess(call, { it?.storeClient == true }) {
            val data = call.receive<CartItemData>()
            if (data.amount <= 0) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val principal = call.principal<UserIdPrincipal>()!!
                cartManagementInterface.addCartItem(principal.name, data.itemId, data.amount)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Товар добавлен в корзину"))
            }
        }
    }

    // Удалить товар из корзины
    delete<CartItemLocation> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.storeClient == true }) {
            val principal = call.principal<UserIdPrincipal>()!!
            if (cartManagementInterface.deleteCartItem(principal.name, data.id)) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Товар удалён из корзины"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Товар не найден"))
            }
        }
    }
}


interface CartManagementInterface {
    fun searchItem(userName: String, itemId: Int): Boolean
    fun getCartItems(userName: String): List<CartItemData>
    fun addCartItem(userName: String, itemId: Int, cartAmount: Int): Boolean
    fun deleteCartItem(userName: String, cartItem: Int): Boolean
    fun clearCart(userName: String): Boolean
}


class CartManagement : CartManagementInterface {
    override fun searchItem(userName: String, itemId: Int): Boolean {
        return transaction {
            val currentUser = Users.select { Users.username eq userName }
            val user = currentUser.single()
            val userId = user[Users.id].value
            val foundItem = Carts.select { (Carts.userId eq userId) and (Carts.itemId eq itemId) }
            return@transaction foundItem.count() > 0
        }
    }

    override fun getCartItems(userName: String): List<CartItemData> {
        return transaction {
            val currentUser = Users.select { Users.username eq userName }
            val user = currentUser.single()
            val userId = user[Users.id].value
            Carts.select { Carts.userId eq userId }.map {
                CartItemData(it[Carts.itemId].value, it[Carts.amount])
            }
        }
    }

    override fun addCartItem(userName: String, itemId: Int, cartAmount: Int): Boolean {
        if (searchItem(userName, itemId)) {
            return transaction {
                val currentUser = Users.select { Users.username eq userName }
                val user = currentUser.single()
                val userId = user[Users.id].value

                Carts.update({ (Carts.userId eq userId) and (Carts.itemId eq itemId) }) {
                    it[amount] = cartAmount
                }
                false
            }
        } else {
            return transaction {
                val currentUser = Users.select { Users.username eq userName }
                val user = currentUser.single()
                val userId = user[Users.id].value
                Carts.insert {
                    it[Carts.userId] = userId
                    it[Carts.itemId] = itemId
                    it[amount] = cartAmount
                }
                true
            }
        }
    }

    override fun deleteCartItem(userName: String, cartItem: Int): Boolean {
        return if (searchItem(userName, cartItem)) {
            transaction {
                val currentUser = Users.select { Users.username eq userName }
                val user = currentUser.single()
                val userId = user[Users.id].value
                Carts.deleteWhere { (Carts.itemId eq cartItem) and (Carts.userId eq userId) }
            }
            true
        } else {
            false
        }
    }

    override fun clearCart(userName: String): Boolean {
        return transaction {
            val currentUser = Users.select { Users.username eq userName }
            val user = currentUser.firstOrNull()
            if (user != null) {
                val userId = user[Users.id].value
                Carts.deleteWhere { (Carts.userId eq userId) }
                true
            } else {
                false
            }
        }
    }
}


class CartManagementFake : CartManagementInterface {
    override fun searchItem(userName: String, itemId: Int): Boolean {
        return if (!TestData.cart.containsKey(userName)) {
            false
        } else {
            val item = TestData.cart[userName]!!.firstOrNull { it.itemId == itemId }
            item != null
        }
    }

    override fun getCartItems(userName: String): List<CartItemData> {
        if (TestData.cart.containsKey(userName)) {
            return TestData.cart[userName]!!
        }
        return listOf()
    }

    override fun addCartItem(userName: String, itemId: Int, cartAmount: Int): Boolean {
        return if (!TestData.cart.containsKey(userName)) {
            false
        } else {
            TestData.cart[userName]!!.add(CartItemData(itemId, cartAmount))
            true
        }
    }

    override fun deleteCartItem(userName: String, cartItem: Int): Boolean {
        return if (searchItem(userName, cartItem)) {
            TestData.cart[userName]!!.removeIf { it.itemId == cartItem }
            true
        } else {
            false
        }
    }

    override fun clearCart(userName: String): Boolean {
        return if (TestData.cart.containsKey(userName)) {
            TestData.cart[userName]?.clear()
            true
        } else {
            false
        }
    }
}