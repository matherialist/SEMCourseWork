package com.holeaf.api

import com.google.gson.Gson
import com.holeaf.api.model.ItemData
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class AccessTest {
    val gson = Gson()

    @Test
    fun testAccessMapSeller() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/district") {
                addHeader("Authorization", getAuthHeader("seller"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessMapCourier() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/district") {
                addHeader("Authorization", getAuthHeader("courier"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessMapManager() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/district") {
                addHeader("Authorization", getAuthHeader("manager"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessMapClient() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/district") {
                addHeader("Authorization", getAuthHeader("client"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessMapSupplierDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/district") {
                addHeader("Authorization", getAuthHeader("supplier"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessMapRegulatorDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/district") {
                addHeader("Authorization", getAuthHeader("regulator"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessKeyRegulator() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/key/client") {
                addHeader("Authorization", getAuthHeader("regulator"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessKeySellerDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/key/client") {
                addHeader("Authorization", getAuthHeader("seller"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessKeySupplierDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/key/client") {
                addHeader("Authorization", getAuthHeader("supplier"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessKeyCourierDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/key/client") {
                addHeader("Authorization", getAuthHeader("courier"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessKeyManagerDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/key/client") {
                addHeader("Authorization", getAuthHeader("manager"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessKeyClientDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/key/client") {
                addHeader("Authorization", getAuthHeader("client"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessStoreSeller() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/store") {
                addHeader("Authorization", getAuthHeader("seller"))
                this.setBody(gson.toJson(ItemData(1, "test", 100, 10, "test", "")))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessStoreClient() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/store") {
                addHeader("Authorization", getAuthHeader("client"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessStoreSupplierDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/store") {
                addHeader("Authorization", getAuthHeader("supplier"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessStoreRegulatorDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/store") {
                addHeader("Authorization", getAuthHeader("regulator"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessStoreManagerDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/store") {
                addHeader("Authorization", getAuthHeader("manager"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessStoreCourier() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/store") {
                addHeader("Authorization", getAuthHeader("courier"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessReportsSeller() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/dailystat/2020-12-25/2020-12-31") {
                addHeader("Authorization", getAuthHeader("seller"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessReportsRegulator() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/dailystat/2020-12-25/2020-12-31") {
                addHeader("Authorization", getAuthHeader("regulator"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessReportsManagerDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/dailystat/2020-12-25/2020-12-31") {
                addHeader("Authorization", getAuthHeader("manager"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessReportsSupplierDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/dailystat/2020-12-25/2020-12-31") {
                addHeader("Authorization", getAuthHeader("supplier"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessReportsCourierDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/dailystat/2020-12-25/2020-12-31") {
                addHeader("Authorization", getAuthHeader("courier"))
            }.apply { assertNull(response.content) }
        }
    }

    @Test
    fun testAccessReportsClientDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/dailystat/2020-12-25/2020-12-31") {
                addHeader("Authorization", getAuthHeader("client"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessSupplySeller() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply") {
                addHeader("Authorization", getAuthHeader("seller"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessSupplySupplier() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply") {
                addHeader("Authorization", getAuthHeader("supplier"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessSupplyRegulatorDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply") {
                addHeader("Authorization", getAuthHeader("regulator"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessSupplyManagerDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply") {
                addHeader("Authorization", getAuthHeader("manager"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessSupplyCourierDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply") {
                addHeader("Authorization", getAuthHeader("courier"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessSupplyClientDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/supply") {
                addHeader("Authorization", getAuthHeader("client"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessTelemetrySeller() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/telemetry") {
                addHeader("Authorization", getAuthHeader("seller"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessTelemetryRegulator() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/telemetry") {
                addHeader("Authorization", getAuthHeader("regulator"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessTelemetryClientDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/telemetry") {
                addHeader("Authorization", getAuthHeader("client"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessTelemetrySupplierDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/telemetry") {
                addHeader("Authorization", getAuthHeader("supplier"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessTelemetryManagerDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/telemetry") {
                addHeader("Authorization", getAuthHeader("manager"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessTelemetryCourier() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/telemetry") {
                addHeader("Authorization", getAuthHeader("courier"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessOrderCourier() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order") {
                addHeader("Authorization", getAuthHeader("courier"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessOrderClient() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order") {
                addHeader("Authorization", getAuthHeader("client"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessOrderSellerDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order") {
                addHeader("Authorization", getAuthHeader("seller"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessOrderSupplierDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order") {
                addHeader("Authorization", getAuthHeader("supplier"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessOrderRegulatorDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order") {
                addHeader("Authorization", getAuthHeader("regulator"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessOrderManagerDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/order") {
                addHeader("Authorization", getAuthHeader("manager"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }

    @Test
    fun testAccessChatClient() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/user/chats") {
                addHeader("Authorization", getAuthHeader("client"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessChatSeller() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/user/chats") {
                addHeader("Authorization", getAuthHeader("seller"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessChatCourier() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/user/chats") {
                addHeader("Authorization", getAuthHeader("courier"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessChatManager() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/user/chats") {
                addHeader("Authorization", getAuthHeader("manager"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessChatSupplier() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/user/chats") {
                addHeader("Authorization", getAuthHeader("supplier"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAccessChatRegulatorDenied() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/user/chats") {
                addHeader("Authorization", getAuthHeader("regulator"))
            }.apply { assertEquals(HttpStatusCode.Forbidden, response.status()) }
        }
    }
}