import com.holeaf.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.content.*
import io.ktor.request.*
import java.io.ByteArrayOutputStream
import java.util.*

data class Photo(val photo:String)

class Profile : StubPage() {

    companion object {
        fun register() {
            PageCollection.register(Profile())
        }
    }

    override fun getUrl(): String {
        return "/profile"
    }

    override suspend fun get(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        val profile = httpClient.get<ProfileResult>("$prefix/user") {
            authorization(call)
        }
        val roles = httpClient.get<List<Role>>("$prefix/roles") {
            authorization(call)
        }
        val photo = "data:image/jpeg;base64," + profile.photo
        val roleName = roles.firstOrNull { it.id==profile.role }?.name.orEmpty()

        return "profile.ftl" to mapOf("profile" to profile, "rolename" to roleName, "photo" to photo)
    }

    override suspend fun post(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val multipart = call.receiveMultipart()
        var photo = byteArrayOf()
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val bar = ByteArrayOutputStream()
                    part.streamProvider().use { input -> bar.buffered().use { output -> input.copyTo(output) } }
                    photo = bar.toByteArray()
                }
            }
            part.dispose()
        }
        httpClient.put<String>("$prefix/user") {
            val json = defaultSerializer()
            authorization(call)
            body = json.write(Photo(Base64.getEncoder().encodeToString(photo)))
        }
        return get(httpClient, call)
    }
}

