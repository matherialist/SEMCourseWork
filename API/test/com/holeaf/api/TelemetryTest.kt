package com.holeaf.api

import com.google.gson.Gson
import com.holeaf.api.model.TelemetryData
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals


class TelemetryTest {
    val gson = Gson()

    @Test
    fun testGetTelemetryList() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/telemetry") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetTelemetry() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/telemetry/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetTelemetryNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/telemetry/4") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testAddTelemetry() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/telemetry") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(TelemetryData(2, "")))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAddTelemetryExisting() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/telemetry") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(TelemetryData(1, "")))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAddTelemetryIncorrectId() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/telemetry") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(TelemetryData(-1, "")))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.BadRequest, response.status()) }
        }
    }

    @Test
    fun testDeleteTelemetry() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/telemetry/2") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testDeleteTelemetryNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/telemetry/3") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testDeleteTelemetryIncorrectId() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/telemetry/-1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.BadRequest, response.status()) }
        }
    }
}