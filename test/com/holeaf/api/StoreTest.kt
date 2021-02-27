package com.holeaf.api

import com.google.gson.Gson
import com.holeaf.api.model.ItemData
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals


class StoreTest{
    val gson = Gson()

    @Test
    fun testGetItemFromStore() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/store/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetItemFromStoreNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/store/98") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testGetGoodsListFromStore() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/store") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAddItemToStore() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/store") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(ItemData(2, "test", 500, 100, "", "")))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAddItemToStoreExisting() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/store") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(ItemData(1, "test", 500, 100, "", "")))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAddItemToStoreNegativePrice() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/store") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(ItemData(1, "test", -100, 100, "", "")))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.BadRequest, response.status()) }
        }
    }

    @Test
    fun testAddItemToStoreNegativeAmount() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/store") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(ItemData(1, "test", 100, -100, "", "")))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.BadRequest, response.status()) }
        }
    }

    @Test
    fun testDeleteItemFromStore() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/store/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testDeleteItemFromStoreNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/store/99") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }
}