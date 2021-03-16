package com.holeaf.api.API

import PlaceInfo
import com.holeaf.api.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


@Location("/place")
class RESTPlace


interface PlaceManagementInterface {
    fun savePlace(userName: String, place: PlaceInfo): Int?
    fun getPlace(userName: String): PlaceInfo?
    fun getAllPlaces(): List<PlaceInfo>
}

class PlaceManagement : PlaceManagementInterface {
    override fun savePlace(userName: String, place: PlaceInfo): Int? {
        return transaction {
            val users = Users.select { Users.username eq userName }
            val userObj = users.single()
            val userId = userObj[Users.id]
            val current = Place.select {
                Place.user eq userId
            }.singleOrNull()
            if (current != null) {
                //update current
                Place.update({ Place.user eq userId }) {
                    it[longitude] = place.longitude
                    it[latitude] = place.latitude
                }
                current[Place.id].value
            } else {
                try {
                    val id = Place.insert {
                        it[user] = userId
                        it[longitude] = place.longitude
                        it[latitude] = place.latitude
                    } get Place.id
                    id.value
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override fun getPlace(userName: String): PlaceInfo? {
        val users = Users.select { Users.username eq userName }
        val userObj = users.single()
        val userId = userObj[Users.id].value
        return try {
            transaction {
                var place = Place.select {
                    Place.user eq userId
                }.orderBy(Place.id, SortOrder.DESC).single()
                PlaceInfo(place[Place.id].value, userId, place[Place.longitude], place[Place.latitude])
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun getAllPlaces(): List<PlaceInfo> {
        return transaction {
            val roles = Roles.select { Roles.mapCourier eq true }.map { it[Roles.id] }
            val users = Users.select { Users.roleId inList roles }
            val places = users.map { user ->
                var place = Place.select {
                    Place.user eq user[Users.id].value
                }.orderBy(Place.id, SortOrder.DESC).singleOrNull()
                if (place != null) {
                    PlaceInfo(
                        place[Place.id].value,
                        user[Users.id].value,
                        place[Place.longitude],
                        place[Place.latitude]
                    )
                } else {
                    null
                }
            }
            places.filter {
                it != null
            }.map {
                it!!
            }
        }
    }
}

fun Route.places(
    userManagementInterface: UserManagementInterface,
    placeManagementInterface: PlaceManagementInterface,
    orderManagementInterface: OrderManagementInterface,
) {
    post<RESTPlace> {
        holeafApplication.validationInterface.checkAccess(
            call,
            { it?.mapCourier == true || it?.mapManager == true || it?.mapClient == true || it?.mapSeller == true }) {
            val data = call.receive<PlaceInfo>()
            val principal = call.principal<UserIdPrincipal>()!!
            val user = userManagementInterface.getUser(principal.name)
            val roles = userManagementInterface.getRoles()
            val orders = orderManagementInterface.getOrders(principal.name)
            val courierName = orders.firstOrNull { it.status == 1 }?.courierName.orEmpty()
            val courierId = userManagementInterface.getUser(courierName)?.id

            val role = roles.firstOrNull { it.id == user?.role }
            var placeId: Int? = -1
            if (role?.mapCourier == true) {
                placeId = placeManagementInterface.savePlace(principal.name, data)
            }
            val couriersNearMe = mutableListOf<PlaceInfo>()
            if (placeId != null) {
                val allLocations = placeManagementInterface.getAllPlaces()
                allLocations.forEach { place ->
                    if (place.id != placeId) {
//                        print("\nPLACE ${place.id}")
//                        print("\nLat: ${place.latitude}, Long: ${place.longitude}")
//                        print("\nMLAt: ${data.latitude}, Long: ${data.longitude}")
                        //don't compare to ourselves
                        val lat_a = place.latitude.toDouble()/180.0*Math.PI
                        val lat_b = data.latitude.toDouble()/180.0*Math.PI
                        val long_a = place.longitude.toDouble()/180.0*Math.PI
                        val long_b = data.longitude.toDouble()/180.0*Math.PI
                        val d = Math.acos(
                            Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a) * Math.cos(lat_b) * Math.cos(long_a - long_b)
                        )
                        val L = d * 6371          //L - distance in km

//                        print("\nDistance between ${place.user} and me: $L")
                        if ((L <= 0.05 && role?.mapManager == true) || (role?.mapSeller == true) || (role?.mapClient == true && place.user == courierId)) {            //near me
                            print("\nAdding ${place.id} for ${place.user}...")
                            userManagementInterface.getAllUsers().map { user ->
                                val role = roles.firstOrNull { user.role == it.id }
                                if (role != null) {
                                    if (role.mapCourier) couriersNearMe.add(place)
                                }
                            }
                        }
                    }
                }
                call.respond(HttpStatusCode.OK, couriersNearMe)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Пользователь не найден"))
            }
        }
    }

    get<RESTPlace> {
        holeafApplication.validationInterface.checkAccess(call, { it?.mapClient == true }) {
            val principal = call.principal<UserIdPrincipal>()!!
            launch {
                val place = placeManagementInterface.getPlace(principal.name)
                if (place != null) {
                    call.respond(HttpStatusCode.OK, place)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("message" to "Данные не найдены"))
                }
            }
        }
    }
}

class PlaceManagementFake : PlaceManagementInterface {
    override fun savePlace(userName: String, place: PlaceInfo): Int? {
        return if (TestData.users.firstOrNull { it.username == userName } != null) {
            TestData.places.add(place)
            return place.id
        } else {
            null
        }
    }

    override fun getPlace(userName: String): PlaceInfo? {
        val userId = TestData.users.first { it.username == userName }.id
        return TestData.places.firstOrNull { it.user == userId }
    }

    override fun getAllPlaces(): List<PlaceInfo> {
        return TestData.places
    }

}