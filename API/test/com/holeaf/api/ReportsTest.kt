package com.holeaf.api

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals


class ReportsTest {
    val gson = Gson()

    @Test
    fun testGetDailySaleStat() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/dailystat/2020-12-25/2020-12-25") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetProductSaleStat() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/productstat/2020-12-25/2020-12-25") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }
}