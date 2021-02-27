package com.holeaf.api

import com.google.gson.Gson
import com.holeaf.api.API.ChatInfo
import com.holeaf.api.model.MessageData
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals


class ChatsTest {
    val gson = Gson()

    @Test
    fun testGetUserChats() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/user/chats") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply {
                assertEquals(
                    gson.toJson(listOf(ChatInfo("admin", "12345678", 1, "test", "", 0))),
                    response.content
                )
            }
        }
    }

    @Test
    fun testGetChatMessagesSuccess() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/messages/1") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply {
                assertEquals(
                    gson.toJson(listOf(MessageData(1, 1, "00:00", "test"))),
                    response.content
                )
            }
        }
    }

    @Test
    fun testGetChatMessagesFailed() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/messages/2") {
                addHeader("Authorization", getAuthHeader("admin"))
            }.apply { assertEquals(gson.toJson(listOf<MessageData>()), response.content) }
        }
    }
}