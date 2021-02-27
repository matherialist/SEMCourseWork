package com.holeaf.api

import AuthUser
import NewUser
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals


class LoginTest {
    val gson = Gson()

    @Test
    fun testAuth() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/auth") {
                addHeader("Authorization", getAuthHeader("admin"))
                this.setBody(gson.toJson(AuthUser("admin", "12345678")))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }


    @Test
    fun testRegistrationSameUser() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(
                HttpMethod.Post,
                "/user"
            ) {
                val user = NewUser("admin", "test11", "sad2@asd.ru")
                this.setBody(gson.toJson(user))
                this.addHeader("Content-Type", "application/json")
            }.apply {
                assertEquals(HttpStatusCode.Conflict, response.status())
                val message: Message = gson.fromJson(response.content, Message::class.java)
                assertEquals(message.message, "Это имя пользователя уже занято")
            }
        }
    }

    @Test
    fun testRegistrationSameEmail() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(
                HttpMethod.Post,
                "/user"
            ) {
                val user = NewUser("saf", "test11", "admin@test.com")
                this.setBody(gson.toJson(user))
                this.addHeader("Content-Type", "application/json")
            }.apply {
                assertEquals(HttpStatusCode.Conflict, response.status())
                val message: Message = gson.fromJson(response.content, Message::class.java)
                assertEquals(message.message, "Этот адрес электронный почты уже использовался")
            }
        }
    }

    @Test
    fun testRegistrationEmptyLogin() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(
                HttpMethod.Post,
                "/user"
            ) {
                val user = NewUser("", "test11", "elizabethkotelnikova1997+123@gmail.com")
                this.setBody(gson.toJson(user))
                this.addHeader("Content-Type", "application/json")
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testRegistrationEmptyPassword() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(
                HttpMethod.Post,
                "/user"
            ) {
                val user = NewUser("test11", "", "elizabethkotelnikova1997+123@gmail.com")
                this.setBody(gson.toJson(user))
                this.addHeader("Content-Type", "application/json")
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testRegistrationEmptyEmail() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(
                HttpMethod.Post,
                "/user"
            ) {
                val user = NewUser("test11", "test11", "")
                this.setBody(gson.toJson(user))
                this.addHeader("Content-Type", "application/json")
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testRegistrationBadEmail() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(
                HttpMethod.Post,
                "/user"
            ) {
                val user = NewUser("test11", "test11", "elizabethkotelnikova1997+123gmail.com")
                this.setBody(gson.toJson(user))
                this.addHeader("Content-Type", "application/json")
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testRegistrationSuccess() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(
                HttpMethod.Post,
                "/user"
            ) {
                val user = NewUser("test12", "test11", "elizabethkotelnikova@gmail.com")
                this.setBody(gson.toJson(user))
                this.addHeader("Content-Type", "application/json")
            }.apply { assertEquals(HttpStatusCode.OK, response.status()) }
        }
    }
}