import com.fasterxml.jackson.databind.ObjectMapper
import com.holeaf.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.request.*

class Telemetry : CRUDPage {

    override fun getUrl(): String {
        return "/cars"
    }

    companion object {
        fun register() {
            PageCollection.register(Telemetry())
        }
    }

    override suspend fun list(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {

        val cars = httpClient.get<List<Int>>("$prefix/telemetry") {
            authorization(call)
        }
        var next = 1
        if (cars.isNotEmpty()) {
            next = cars.maxOf { it } + 1
        }
        print(cars)
        return "cars.ftl" to mapOf("cars" to cars, "next" to next)
    }

    override suspend fun create(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        val data = call.receiveParameters()
        val id = data["id"]?.toIntOrNull() ?: 0
        val telemetry = data["car-telemetry"] ?: "[]"
        val json = defaultSerializer()
        try {
            val mapper = ObjectMapper()
            mapper.readTree(telemetry)
            if (telemetry.isNotBlank()) {
                httpClient.post<String>("$prefix/telemetry") {
                    authorization(call)
                    body = json.write(TelemetryData(id, telemetry))
                }
                return "/cars" to null
            } else {
                return "/cars/${id}?error=true" to null
            }
        } catch (e: Exception) {
            return "/cars/${id}?error=true" to null
        }
    }

    override suspend fun delete(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        val id = call.parameters["id"]
        httpClient.delete<String>("$prefix/telemetry/$id") {
            authorization(call)
        }
        return "/cars" to null
    }

    override suspend fun read(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        val id = call.parameters["id"]
        var error = call.request.queryParameters["error"].orEmpty()
        if (error != "") {
            error = "Неправильный формат JSON"
        }
        print("Car data for $id")
        val car = httpClient.get<String>("$prefix/telemetry/$id") {
            authorization(call)
        }
        print("Result is $car")
        return "car.ftl" to mapOf("data" to car, "id" to id, "message" to error)
    }

    override suspend fun update(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        TODO("Not yet implemented")
    }

}

