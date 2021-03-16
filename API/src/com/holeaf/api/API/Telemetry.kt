package com.holeaf.api.API

import com.holeaf.api.*
import com.holeaf.api.model.TelemetryData
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

@Location("/telemetry")
class RESTTelemetryList

@Location("/telemetry/{id}")
data class RESTTelemetry(val id: Int)


fun Route.telemetry(telemetryManagementInterface: TelemetryManagementInterface) {
    // Получить телеметрию для выбранной машины

    get<RESTTelemetryList> {
        holeafApplication.validationInterface.checkAccess(call, { it?.telemetry == true || it?.mapCourier == true }) {
            val list = telemetryManagementInterface.getTelemetryList()
            launch {
                call.respond(HttpStatusCode.OK, list)
            }
        }
    }


    get<RESTTelemetry> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.telemetry == true || it?.mapCourier == true }) {
            val id = data.id
            if (telemetryManagementInterface.searchTelemetry(id)) {
                call.respond(HttpStatusCode.OK, telemetryManagementInterface.getTelemetry(id))
            } else {
                launch { call.respond(HttpStatusCode.NotFound, mapOf("message" to "Нет данных о телеметрии")) }
            }
        }
    }

    post<RESTTelemetryList> {
        holeafApplication.validationInterface.checkAccess(call, { it?.telemetry == true }) {
            val telemetryData = call.receive<TelemetryData>()
            if (telemetryData.id <= 0) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                launch {
                    val added = telemetryManagementInterface.addTelemetry(telemetryData.id, telemetryData.data)
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to if (added) "Телеметрия добавлена" else "Телеметрия обновлена")
                    )
                }
            }
        }
    }

    delete<RESTTelemetry> { data ->
        holeafApplication.validationInterface.checkAccess(call, { it?.telemetry == true }) {
            val id = data.id
            if (id <= 0) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                launch {
                    if (telemetryManagementInterface.deleteTelemetry(id)) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Данные телеметрии удалены"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("message" to "Данные телеметрии не найдены"))
                    }
                }
            }
        }
    }
}


interface TelemetryManagementInterface {
    fun getTelemetryList(): List<Int>
    fun searchTelemetry(id: Int): Boolean
    fun getTelemetry(id: Int): String
    fun addTelemetry(id: Int, data: String): Boolean
    fun deleteTelemetry(id: Int): Boolean
}


class TelemetryManagement : TelemetryManagementInterface {
    override fun getTelemetryList(): List<Int> {
        return transaction {
            Telemetry.selectAll().map {
                it[Telemetry.id].value
            }
        }
    }

    override fun searchTelemetry(id: Int): Boolean {
        return transaction {
            val foundTelemetry = Telemetry.select { Telemetry.id eq id }
            return@transaction foundTelemetry.count() > 0
        }
    }

    override fun getTelemetry(id: Int): String {
        return transaction {
            Telemetry.select { Telemetry.id eq id }
                .map { it[Telemetry.data] }.first()
        }
    }

    override fun addTelemetry(id: Int, data: String): Boolean {
        if (searchTelemetry(id)) {
            transaction {
                Telemetry.update({ Telemetry.id eq id }) {
                    it[Telemetry.data] = data
                }
            }
            return false
        } else {
            transaction {
                Telemetry.insert {
                    it[Telemetry.id] = id
                    it[Telemetry.data] = data
                }
            }
            return true
        }

    }

    override fun deleteTelemetry(id: Int): Boolean {
        return if (searchTelemetry(id)) {
            transaction {
                Telemetry.deleteWhere { Telemetry.id eq id }
            }
            true
        } else {
            false
        }
    }
}


class TelemetryManagementFake : TelemetryManagementInterface {
    override fun getTelemetryList(): List<Int> {
        return TestData.telemetry.map { it.id }
    }

    override fun searchTelemetry(id: Int): Boolean {
        return TestData.telemetry.firstOrNull { it.id == id } != null
    }

    override fun getTelemetry(id: Int): String {
        return TestData.telemetry.first { it.id == id }.data
    }

    override fun addTelemetry(id: Int, data: String): Boolean {
        return if (id > 0) {
            if (searchTelemetry(id)) {
                TestData.telemetry.first { it.id == id }.data = data
                true
            } else {
                TestData.telemetry.add(TelemetryData(id, data))
                false
            }
        } else {
            false
        }
    }

    override fun deleteTelemetry(id: Int): Boolean {
        return if (searchTelemetry(id)) {
            TestData.telemetry.removeIf { it.id == id }
            true
        } else {
            false
        }
    }

}