package com.holeaf.api

import com.google.gson.Gson
import com.holeaf.api.API.PlaceData
import com.holeaf.api.model.UserOrderData
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals


class OrderTest {
    val gson = Gson()

    @Test
    fun testGetUserOrders() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetCourierOrders() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order/district") {
                addHeader("Authorization", getAuthHeader("courier"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetCourierOrdersEmpty() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order/district") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(listOf<UserOrderData>(), gson.fromJson(response.content, Array<UserOrderData>::class.java).toList()) }
        }
    }

    @Test
    fun testGetOrderDetails() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetOrderDetailsNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order/5") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testCreateOrder() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/order") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(PlaceData(0F, 0F)))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testChangeOrderStatus() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/order/1/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testChangeOrderStatusOrderNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/order/8/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }
}