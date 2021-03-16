import com.holeaf.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.request.*
import io.ktor.sessions.*

class Keys : StubPage() {
    override fun getUrl(): String {
        return "/keys"
    }

    companion object {
        fun register() {
            PageCollection.register(Keys())
        }
    }

    override suspend fun get(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        val principal = call.sessions.get<TokenPrincipal>()
        val username = call.request.queryParameters["username"].orEmpty()
        val found = if (principal?.found == true) 1 else 0
        var key = principal?.key.orEmpty()
        if (username.isBlank()) key = ""
        return "keys.ftl" to mapOf("key" to key, "found" to found)
    }

    override suspend fun post(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        val principal = call.sessions.get<TokenPrincipal>()
        val post = call.receiveParameters()
        val username = post["username"].orEmpty()
        try {
            val KeyInformation = httpClient.get<KeyInformation>("$prefix/key/$username") {
                authorization(call)
            }
            call.sessions.set(
                TokenPrincipal(
                    key = KeyInformation.key,
                    found = true,
                    token = principal?.token.orEmpty(),
                    role = principal?.role ?: 0,
                    permissions = principal?.permissions?:Role(0, "Клиент", false, false, false, false, true, false, true, false, false, false, true, false, false, false, false, false, false)
                )
            )
        } catch (e: Exception) {
            call.sessions.set(TokenPrincipal(key = username, found = false, token = principal?.token.orEmpty(),                    role = principal?.role ?: 0,
                permissions = principal?.permissions?:Role(0, "Клиент", false, false, false, false, true, false, true, false, false, false, true, false, false, false, false, false, false)
            ))
        }
        return "keys?username=$username" to null
    }
}

