package com.holeaf.api

import com.google.gson.Gson
import com.holeaf.api.API.CartItemData
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals


class CartTest {
    val gson = Gson()

    @Test
    fun testGetCartItems() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/cart") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(gson.toJson(TestData.cart["admin"]), response.content) }
        }
    }

    @Test
    fun testAddCartItem() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/cart") {
                addHeader("Authorization", getAuthHeader("admin"))
                val itemData = CartItemData(2, 10)
                this.setBody(gson.toJson(itemData))
                this.addHeader("Content-Type", "application/json")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testDeleteCartItemSuccess() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/cart/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testDeleteCartItemNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/cart/97") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }
}