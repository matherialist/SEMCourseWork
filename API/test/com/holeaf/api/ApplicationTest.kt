package com.holeaf.api

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.testing.*
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

fun getAuthHeader(userName: String): String {
    return "Basic " + Base64.getEncoder().encodeToString("token:$userName".toByteArray())
}

data class Message(var message: String)

class ApplicationTest {
    val gson = Gson()

    @Test
    fun testPing() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/ping").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("It's OK.", response.content)
            }
        }
    }
}