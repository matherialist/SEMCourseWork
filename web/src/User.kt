import com.holeaf.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.content.*
import io.ktor.request.*
import java.io.ByteArrayOutputStream
import java.util.*

data class UserEditInfo(val id:Int, val user:String, val email:String, val photo:String, val role:Int, var rolename:String?=null)

data class UserModify(val id:Int, val email:String, val role:Int, val photo:String?)

class User : CRUDPage {
    companion object {
        fun register() {
            PageCollection.register(User())
        }
    }

    override suspend fun create(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        TODO("Not yet implemented")
    }

    override suspend fun list(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        val users = httpClient.get<List<UserEditInfo>>("$prefix/everyone") {
            authorization(call)
        }
        var next = 1
        if (users.isNotEmpty()) {
            next = users.maxOf { it.id } + 1
        }
        val roles = httpClient.get<List<Role>>("$prefix/roles")
        users.forEach { user ->
            user.rolename = roles.firstOrNull { it.id==user.role.toInt() }?.name ?: "Неизвестно"
        }

        return "users.ftl" to mapOf("users" to users, "next" to next)
    }

    override suspend fun read(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        val users = httpClient.get<List<UserEditInfo>>("$prefix/everyone") {
            authorization(call)
        }
        val id = call.parameters["id"]?.toIntOrNull() ?: 0
        print("ID=$id")
        var next = 1
        if (users.isNotEmpty()) {
            next = users.maxOf { it.id } + 1
        }
        val roles = httpClient.get<List<Role>>("$prefix/roles")
        users.forEach { user ->
            user.rolename = roles.firstOrNull { it.id==user.role.toInt() }?.name ?: "Неизвестно"
        }

        var error = call.request.queryParameters["error"].orEmpty()
        if (error != "") {
            error = "Ошибка при редактировании пользователя"
        }
        val user = users.firstOrNull { it.id == id }
        print("\nData is $user\n")
        //get photo!
        val photo = "data:image/jpeg;base64,"+user?.photo
        return "user.ftl" to
                mapOf(
                    "users" to users,
                    "data" to user,
                    "photo" to photo,
                    "message" to error,
                    "next" to next,
                    "roles" to roles,
                )
    }

    override suspend fun update(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        val multipart = call.receiveMultipart()

        var id = 0
        var email = ""
        var role = 0
        var photo:ByteArray? = null

        print("Parsing multipart $multipart\n")
        multipart.forEachPart { part ->
            print(part)
            when (part) {
                is PartData.FormItem -> {
                    print("FIT ${part.name}\n")
                    when (part.name) {
                        "id" -> id = part.value.toIntOrNull() ?: 0
                        "email" -> email = part.value
                        "role" -> role = part.value.toIntOrNull() ?: 0
                    }
                }
                is PartData.FileItem -> {
                    val bar = ByteArrayOutputStream()
                    part.streamProvider().use { input -> bar.buffered().use { output -> input.copyTo(output) } }
                    photo = bar.toByteArray()
                }
            }
            part.dispose()
        }

        val json = defaultSerializer()
        httpClient.put<String>("$prefix/user/modify") {
            authorization(call)
            val photoData = photo?.let { Base64.getEncoder().encodeToString(it)}
            print("Photo is $photoData\n")
            val modificationObject = UserModify(id, email, role, photoData)
            body = json.write(modificationObject)
        }
        return "/users" to null
    }

    override suspend fun delete(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        TODO("Not yet implemented")
    }

    override fun getUrl(): String {
        return "/users"
    }
}
