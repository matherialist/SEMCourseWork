package com.holeaf.api

import com.google.gson.Gson
import com.holeaf.api.API.DistrictData
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals


class DistrictTest {
    val gson = Gson()

    @Test
    fun testGetDistrictList() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/district") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(gson.toJson(TestData.districts), response.content) }
        }
    }

    @Test
    fun testGetDistrictSuccess() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/district/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetDistrictFailed() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/district/3") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testAddDistrict() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/district") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(
                    gson.toJson(
                        DistrictData(
                            2, "test", 0F, 0F, 1F, 1F,
                            listOf()
                        )
                    )
                )
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAddDistrictExisting() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/district") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(
                    gson.toJson(
                        DistrictData(
                            1, "test", 0F, 0F, 1F, 1F,
                            listOf()
                        )
                    )
                )
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAddDistrictIncorrectId() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/district") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(
                    gson.toJson(
                        DistrictData(
                            -1, "test", 0F, 0F, 1F, 1F,
                            listOf()
                        )
                    )
                )
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.BadRequest, response.status()) }
        }
    }

    @Test
    fun testDeleteDistrict() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/district/2") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testDeleteDistrictNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/district/3") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testDeleteDistrictIncorrectId() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/district/-1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.BadRequest, response.status()) }
        }
    }

    @Test
    fun testAddCourierToDistrict() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/district/1/5") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testAddCourierToDistrictDistrictNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/district/3/5") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testAddCourierToDistrictCourierNotExists() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/district/1/4") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testAddCourierToDistrictCourierAlreadyAdded() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/district/1/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }

    @Test
    fun testDeleteCourierFromDistrict() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/district/1/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }
}