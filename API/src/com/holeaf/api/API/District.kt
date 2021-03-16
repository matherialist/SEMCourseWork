package com.holeaf.api.API

import com.holeaf.api.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Location("/district")
class RESTDistrictList

@Location("/district/{id}")
data class RESTDistrict(val id: Int)

@Location("/district/{id}/{courier}")
data class RESTDistrictCourier(val id: Int, val courier: Int)

data class DistrictData(
    val id: Int,
    var title: String,
    val longitude_top: Float,
    val latitude_top: Float,
    val longitude_bot: Float,
    val latitude_bot: Float,
    var couriers: List<Int>
)

fun Route.district(districtManagementInterface: DistrictManagementInterface) {
    // Получить данные по районам и курьерам

    get<RESTDistrictList> {
        holeafApplication.validationInterface.checkAccess(
            call,
            { it?.mapClient == true || it?.mapCourier == true || it?.mapManager == true || it?.mapSeller == true }) {
            print("Get district list")
            val list = districtManagementInterface.getDistrictList()
            print("List is $list")
            launch {
                call.respond(HttpStatusCode.OK, list)
            }
        }
    }


    get<RESTDistrict> { data ->
        holeafApplication.validationInterface.checkAccess(
            call,
            { it?.mapClient == true || it?.mapCourier == true || it?.mapManager == true || it?.mapSeller == true }) {
            val id = data.id
            if (districtManagementInterface.searchDistrict(id)) {
                call.respond(HttpStatusCode.OK, districtManagementInterface.getDistrict(id))
            } else {
                launch { call.respond(HttpStatusCode.NotFound, mapOf("message" to "Нет данных о районах")) }
            }
        }
    }

    post<RESTDistrictList> {
        holeafApplication.validationInterface.checkAccess(call, {
            it?.mapClient == true || it?.mapCourier == true || it?.mapManager == true || it?.mapSeller == true
        }) {
            val districtData = call.receive<DistrictData>()
            if (districtData.id < 0) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                launch {
                    val added = districtManagementInterface.addDistrict(districtData)
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to if (added) "Информация о районах добавлена" else "Информация о районах обновлена")
                    )
                }
            }
        }
    }

    delete<RESTDistrict> { data ->
        holeafApplication.validationInterface.checkAccess(
            call,
            { it?.mapClient == true || it?.mapCourier == true || it?.mapManager == true || it?.mapSeller == true }) {
            val id = data.id
            if (id < 0) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                launch {
                    if (districtManagementInterface.deleteDistrict(id)) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Данные о районах удалены"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("message" to "Данные о районах не найдены"))
                    }
                }
            }
        }
    }

    post<RESTDistrictCourier> { data ->
        holeafApplication.validationInterface.checkAccess(
            call,
            { it?.mapClient == true || it?.mapCourier == true || it?.mapManager == true || it?.mapSeller == true }) {
            launch {
                if (districtManagementInterface.addDistrictCourier(data.id, data.courier)) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "Курьер успешно присоединен"))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("message" to "Курьер или район не найдены, или такой курьер уже присоединён.")
                    )
                }
            }
        }
    }

    delete<RESTDistrictCourier> { data ->
        holeafApplication.validationInterface.checkAccess(
            call,
            { it?.mapClient == true || it?.mapCourier == true || it?.mapManager == true || it?.mapSeller == true }) {
            if (districtManagementInterface.removeDistrictCourier(data.id, data.courier)) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Курьер успешно отсоединен"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to "Курьер не найден"))
            }
        }
    }
}


interface DistrictManagementInterface {
    fun getDistrictList(): List<DistrictData>
    fun searchDistrict(id: Int): Boolean
    fun getDistrict(id: Int): DistrictData
    fun addDistrict(district: DistrictData): Boolean
    fun deleteDistrict(id: Int): Boolean
    fun addDistrictCourier(id: Int, courierId: Int): Boolean
    fun removeDistrictCourier(id: Int, courierId: Int): Boolean
    fun detectDistrict(latitude: Float, longitude: Float): Int?
}

class DistrictManagement : DistrictManagementInterface {
    override fun getDistrictList(): List<DistrictData> {
        return transaction {
            Districts.selectAll().map {
                DistrictData(
                    it[Districts.id].value,
                    it[Districts.title],
                    it[Districts.longitudeTop],
                    it[Districts.latitudeTop],
                    it[Districts.longitudeBot],
                    it[Districts.latitudeBot],
                    listOf()
                )
            }
        }
    }

    override fun searchDistrict(id: Int): Boolean {
        return transaction {
            val foundDistrict = Districts.select { Districts.id eq id }
            return@transaction foundDistrict.count() > 0
        }
    }

    override fun getDistrict(id: Int): DistrictData {
        return transaction {
            val obj = Districts.innerJoin(DistrictCouriers).select { Districts.id eq id }
            val couriers = obj.map { it[DistrictCouriers.courier].value }
            Districts.select { Districts.id eq id }
                .map {
                    DistrictData(
                        it[Districts.id].value,
                        it[Districts.title],
                        it[Districts.longitudeTop],
                        it[Districts.latitudeTop],
                        it[Districts.longitudeBot],
                        it[Districts.latitudeBot],
                        couriers
                    )
                }.first()
        }
    }

    override fun addDistrictCourier(id: Int, courierId: Int): Boolean {
        return transaction {
            //check courier
            val usersCount = Users.select { Users.id eq courierId }.count()
            print("UsersCount = $usersCount")
            val districtCount = Districts.select { Districts.id eq id }.count()
            print("DistrictCount $districtCount")
            if (usersCount == 0L || districtCount == 0L) false else {
                DistrictCouriers.insert {
                    it[courier] = courierId
                    it[district] = id
                }
                true
            }
        }
    }

    override fun removeDistrictCourier(id: Int, courierId: Int): Boolean {
        print("ID: $id")
        print("CourierID: $courierId")
        transaction {
            DistrictCouriers.deleteWhere {
                (DistrictCouriers.courier eq courierId) and (DistrictCouriers.district eq id)
            }
        }
        return true
    }

    override fun detectDistrict(latitude: Float, longitude: Float): Int? {
        //get last location
        return try {
            transaction {
                //определение района
                val bounds = Districts.selectAll().filter {
                    ((latitude - it[Districts.latitudeTop]) * (latitude - it[Districts.latitudeBot]) < 0) &&
                            ((longitude - it[Districts.longitudeTop]) * (longitude - it[Districts.longitudeBot]) < 0)
                }.firstOrNull()
                bounds?.get(Districts.id)?.value
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun addDistrict(district: DistrictData): Boolean {
        print("District is $district")
        if (searchDistrict(district.id)) {
            transaction {
                Districts.update({ Districts.id eq district.id }) {
                    it[title] = district.title
                    it[longitudeTop] = district.longitude_top
                    it[latitudeTop] = district.latitude_top
                    it[longitudeBot] = district.longitude_bot
                    it[latitudeBot] = district.latitude_bot
                }
            }
            return false
        } else {
            transaction {
                Districts.insert {
                    it[title] = district.title
                    it[longitudeTop] = district.longitude_top
                    it[latitudeTop] = district.latitude_top
                    it[longitudeBot] = district.longitude_bot
                    it[latitudeBot] = district.latitude_bot
                }
            }
            return true
        }
    }

    override fun deleteDistrict(id: Int): Boolean {
        return if (searchDistrict(id)) {
            transaction {
                Districts.deleteWhere { Districts.id eq id }
            }
            true
        } else {
            false
        }
    }
}


class DistrictManagementFake : DistrictManagementInterface {
    override fun getDistrictList(): List<DistrictData> {
        return TestData.districts
    }

    override fun searchDistrict(id: Int): Boolean {
        return TestData.districts.firstOrNull { it.id == id } != null
    }

    override fun getDistrict(id: Int): DistrictData {
        return TestData.districts.firstOrNull { it.id == id }!!
    }

    override fun addDistrict(district: DistrictData): Boolean {
        if (searchDistrict(district.id)) {
            TestData.districts.removeIf { it.id == district.id }
            TestData.districts.add(district)
        } else {
            TestData.districts.add(district)
        }
        return searchDistrict(district.id)
    }

    override fun deleteDistrict(id: Int): Boolean {
        return if (searchDistrict(id)) {
            TestData.districts.removeIf { it.id == id }
            true
        } else {
            false
        }
    }

    override fun addDistrictCourier(id: Int, courierId: Int): Boolean {
        return if (searchDistrict(id)) {
            if (TestData.users.firstOrNull { it.id == courierId && it.role == CourierRole.id } != null) {
                if (TestData.districts.find { it.id == id }!!.couriers.contains(courierId)) {
                    false
                } else {
                    val district = TestData.districts.find { it.id == id }!!
                    val newCouriers = mutableListOf<Int>()
                    newCouriers.addAll(district.couriers)
                    newCouriers.add(courierId)
                    district.couriers = newCouriers
                    true
                }
            } else {
                false
            }
        } else {
            false
        }
    }

    override fun removeDistrictCourier(id: Int, courierId: Int): Boolean {
        return if (TestData.districts.firstOrNull { it.id == id }?.couriers?.contains(courierId) != null) {
            val district = TestData.districts.find { it.id == id }!!
            val newCouriers = mutableListOf<Int>()
            newCouriers.addAll(district.couriers)
            newCouriers.remove(courierId)
            print("\n" + newCouriers)
            district.couriers = newCouriers
            true
        } else {
            false
        }
    }

    override fun detectDistrict(latitude: Float, longitude: Float): Int? {
        TODO("Not yet implemented")
    }

}