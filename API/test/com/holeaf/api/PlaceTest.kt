package com.holeaf.api

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals
import PlaceInfo


class PlaceTest {
    val gson = Gson()

    @Test
    fun testSavePlace() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/place") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(PlaceInfo(1,1, 0F, 0F)))
                this.addHeader("Content-Type", "application/json")
            }.apply {
                print(response.status())
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testGetPlace() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/place") {
                addHeader("Authorization", getAuthHeader("client"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetPlaceNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/place") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }
}