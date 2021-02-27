package com.holeaf.api

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.testing.*
import junit.framework.Assert
import org.junit.Test
import kotlin.test.assertEquals


class KeysTest {
    val gson = Gson()

    @Test
    fun testGetKey() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/key/admin") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }

    @Test
    fun testGetKeyUserNotFound() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/key/odmen") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(HttpStatusCode.NotFound, response.status()) }
        }
    }
}