package com.holeaf.api

import com.google.gson.Gson
import com.holeaf.api.API.SupplyContractItemData
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals


class SupplyTest {
    val gson = Gson()

    @Test
    fun testGetSupplyContracts() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetContractDetails() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetContractDetailsNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply/3") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testSetContractStatus() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply/status/1/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testSetContractStatusNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply/status/3/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testAddItemToContract() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/supply/1/3") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(SupplyContractItemData(3, 100, 500)))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAddItemToContractContractNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/supply/3/2") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(SupplyContractItemData(2, 10, 400)))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testAddItemToContractItemExists() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/supply/1/1") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(SupplyContractItemData(1, 10, 500)))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testDeleteItemFromContract() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/supply/1/2") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testDeleteItemFromContractItemNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/supply/1/4") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testDeleteItemFromContractNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/supply/3/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testDeleteContract() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/supply/2") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testDeleteContractNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/supply/3") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }
}