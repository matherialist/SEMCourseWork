package com.holeaf.api.API

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


data class Photo(val photo: String)

data class UserProfile(val id: Int, val user: String, val email: String, val photo: String, val role: Int)

fun Route.profile(userManagementInterface: UserManagementInterface) {
    get<UserByIdLocation> { data ->
        //получение частичных данных (имя и фотография) для чата
        val userInformation = userManagementInterface.getUserById(data.id)
        if (userInformation != null) {
            call.respond(HttpStatusCode.OK, userInformation)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    get<UserLocation> {
        val principal = call.principal<UserIdPrincipal>()!!
        val user = userManagementInterface.getUser(principal.name)
        if (user != null) {
            call.respond(HttpStatusCode.OK, user)
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }


    put<UserLocation> {
        val principal = call.principal<UserIdPrincipal>()!!
        val data = call.receive<Photo>()
        call.respond(
            if (userManagementInterface.setPhoto(
                    principal.name,
                    data.photo
                )
            ) HttpStatusCode.OK else HttpStatusCode.InternalServerError
        )
    }
}

