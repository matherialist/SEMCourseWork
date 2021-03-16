import com.holeaf.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.request.*

class District : CRUDPage {
    companion object {
        fun register() {
            PageCollection.register(District())
        }
    }

    override suspend fun create(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val data = call.receiveParameters()
        val id = data["id"]?.toIntOrNull() ?: 0
        val title = data["title"] ?: ""
        val latitude_top = data["latitude_top"]?.toFloatOrNull() ?: 0.0f
        val longitude_top = data["longitude_top"]?.toFloatOrNull() ?: 0.0f
        val latitude_bot = data["latitude_bot"]?.toFloatOrNull() ?: 0.0f
        val longitude_bot = data["longitude_bot"]?.toFloatOrNull() ?: 0.0f
        val json = defaultSerializer()
        var couriers = listOf<Int>()
        try {
            couriers = httpClient.get<DistrictResult>("$prefix/district/$id") {
                authorization(call)
            }.couriers
        } catch (e: Exception) { }

        try {
            httpClient.post<String>("$prefix/district") {
                val result = DistrictResult(
                    id,
                    title,
                    longitude_top,
                    latitude_top,
                    longitude_bot,
                    latitude_bot,
                    couriers
                )
                print(result)
                authorization(call)
                body = json.write(result)
            }
            return "/districts" to null
        } catch (e: Exception) {
            return "/districts/${id}?error=true" to null
        }
    }

    override suspend fun list(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val districts = httpClient.get<List<DistrictResult>>("$prefix/district") {
            authorization(call)
        }
        var next = 1
        if (districts.isNotEmpty()) {
            next = districts.maxOf { it.id } + 1
        }
        return "districts.ftl" to mapOf("districts" to districts, "next" to next)
    }

    override suspend fun read(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val districts = httpClient.get<List<DistrictResult>>("$prefix/district") {
            authorization(call)
        }
        var next = 1
        if (districts.isNotEmpty()) {
            next = districts.maxOf { it.id } + 1
        }
        val all = httpClient.get<List<UserListResult>>("$prefix/user/couriers") {
            authorization(call)
        }.toMutableList()
        val id = call.parameters["id"]
        var error = call.request.queryParameters["error"].orEmpty()
        if (error != "") {
            error = "Ошибка при редактировании района"
        }
        val district = httpClient.get<DistrictResult>("$prefix/district/$id") {
            authorization(call)
        }
        val couriers = mutableListOf<CourierInfo>()
        district.couriers.forEach { courier ->
            val found = all.firstOrNull { it.id == courier }
            if (found != null) {
                couriers.add(CourierInfo(found.id, found.login))
                all.removeIf { it.id == found.id }
            }
        }
        return "district.ftl" to
                mapOf(
                    "districts" to districts,
                    "data" to district,
                    "id" to id,
                    "message" to error,
                    "next" to next,
                    "couriers" to couriers,
                    "all" to all
                )
    }

    override suspend fun update(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        val id = call.parameters["id"]
        httpClient.delete<String>("$prefix/district/$id") {
            authorization(call)
        }
        return "/districts" to null
    }

    override fun getUrl(): String {
        return "/districts"
    }
}

class DistrictCourierAddition : StubPage() {
    override fun getUrl() = "/districts/{id}/courier"

    companion object {
        fun register() {
            PageCollection.register(DistrictCourierAddition())
        }
    }

    override suspend fun post(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val data = call.receiveParameters()
        print(data)
        val id = data["id"].orEmpty()
        val courier = data["courier"].orEmpty()
        print("ID=$id\n")
        print("Courier=$courier\n")
        httpClient.post<String>("$prefix/district/${id}/${courier}") {
            authorization(call)
        }
        return "/districts/${id}" to null
    }
}

class DistrictCourierDeletion : StubPage() {

    override fun getUrl() = "/districts/{id}/{courier}/delete"

    companion object {
        fun register() {
            PageCollection.register(DistrictCourierDeletion())
        }
    }

    override suspend fun get(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val id = call.parameters["id"]
        val courier = call.parameters["courier"]
        httpClient.delete<String>("$prefix/district/${id}/${courier}") {
            authorization(call)
        }
        return "/districts/${id}" to null
    }
}
