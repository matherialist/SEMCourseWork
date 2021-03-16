package com.holeaf

import District
import DistrictCourierAddition
import DistrictCourierDeletion
import GoodsFromSupplyDeletion
import GoodsToSupplyAddition
import Keys
import Profile
import Report
import Supply
import Telemetry
import User
import com.fasterxml.jackson.databind.SerializationFeature
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import java.util.*
import kotlin.collections.set

fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

object Cookies {
    const val AUTH_COOKIE = "auth"
}

object CommonRoutes {
    const val LOGIN = "/login"
    const val LOGOUT = "/logout"
    const val PROFILE = "/profile"
}

object AuthName {
    const val SESSION = "auth_session"
    const val FORM = "auth_form"
}

object FormFields {
    const val USERNAME = "username"
    const val PASSWORD = "password"
}

var prefix:String = ""
//val prefix = "http://localhost:10080"

data class AuthRequest(val login: String, val password: String)
data class AuthResult(val id: String, val token: String, val role:Int, val permissions:Role)

data class ProfileResult(val id: String, val user: String, val email: String, val role:Int, val photo:String)

data class UserListResult(val id: Int, val login: String, val email: String)

data class CourierInfo(val id: Int, val login: String)

data class DistrictResult(
    val id: Int,
    val title: String,
    val longitude_top: Float,
    val latitude_top: Float,
    val longitude_bot: Float,
    val latitude_bot: Float,
    val couriers: List<Int>
)

data class KeyInformation(val key: String)

val httpClient = HttpClient(OkHttp) {
    install(JsonFeature) {
        serializer = JacksonSerializer()
    }
}

data class TokenPrincipal(val token: String, var key: String, var found: Boolean, var role:Int, var permissions:Role) : Principal

data class TelemetryData(var id: Int, var data: String)

data class Role(
    val id: Int,
    val name: String,
    val chatEveryone: Boolean,
    val chatCourier: Boolean,
    val chatSupplier: Boolean,
    val chatManager: Boolean,
    val chatClient: Boolean,
    val chatSeller: Boolean,
    val mapClient: Boolean,
    val mapSeller: Boolean,
    val mapManager: Boolean,
    val mapCourier: Boolean,
    val storeClient: Boolean,
    val storeSeller: Boolean,
    val supplies: Boolean,
    val orders: Boolean,
    val telemetry: Boolean,
    val stats: Boolean,
    val keys: Boolean,
    val users: Boolean? = false
)

object AuthProvider {
    suspend fun tryAuth(userName: String, password: String): TokenPrincipal? {
        //Here you can use DB or other ways to check user and create a Principal
        val json = defaultSerializer()

        return try {
            print("Connecting to $prefix")
            val result = httpClient.post<AuthResult>("$prefix/auth") {
                body = json.write(AuthRequest(userName, password))
            }
            val perm = result.permissions
            print("Result=$result")
            if (perm.storeSeller || perm.telemetry || perm.supplies || perm.users==true || perm.mapClient || perm.mapCourier) {
                print("\nRole is ${result.role}")
                print("\nPermissions is ${result.permissions}")
                TokenPrincipal(result.token, "", false, result.role, result.permissions)
            } else {
                null
            }
        } catch (e: Exception) {
            print("Exception")
            e.printStackTrace()
            null
        }
    }
}

fun HttpRequestBuilder.authorization(call: ApplicationCall) {
    val cl = call.principal<TokenPrincipal>()
    val auth = Base64.getEncoder().encodeToString("token:${cl?.token}".toByteArray())
    this.header("Authorization", "Basic $auth")
}

suspend fun processPageResult(call: ApplicationCall, data: Pair<String, Map<String, Any?>?>) {
    val principal = call.principal<TokenPrincipal>()
    val permissions = principal?.permissions
    permissions?.let { permissions ->
        if (data.second == null) {
            //redirect
            call.respondRedirect(data.first)
        } else {
            if (data.second is Map) {
                val map = (data.second as Map).toMutableMap()
                map["permissions"] = permissions
                call.respond(FreeMarkerContent(data.first, map))
            } else {
                call.respond(FreeMarkerContent(data.first, data.second))
            }
        }
    }
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    prefix = if (testing) "https://hw.dzolotov.tech/apitest" else "https://hw.dzolotov.tech/api"
//    prefix = "http://localhost:10080"
    print("Prefix is $prefix")
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(Locations) {
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }



    install(Sessions) {
        cookie<TokenPrincipal>(
            Cookies.AUTH_COOKIE,
            storage = SessionStorageMemory()
        ) {
            cookie.path = "/"
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        anyHost()
    }

    install(Authentication) {
        session<TokenPrincipal>(AuthName.SESSION) {
            challenge {
                // What to do if the user isn't authenticated
                call.respondRedirect("${CommonRoutes.LOGIN}?no")
            }
            validate { session: TokenPrincipal ->
                // If you need to do additional validation on session data, you can do so here.
                session
            }
        }

        form(AuthName.FORM) {
            userParamName = FormFields.USERNAME
            passwordParamName = FormFields.PASSWORD
            challenge {
                // I don't think form auth supports multiple errors, but we're conservatively assuming there will be at
                // most one error, which we handle here. Worst case, we just send the user to login with no context.
                val errors: Map<Any, AuthenticationFailedCause> = call.authentication.errors
                print(errors.values.toString())
                when (errors.values.singleOrNull()) {
                    AuthenticationFailedCause.InvalidCredentials ->
                        call.respondRedirect("${CommonRoutes.LOGIN}?invalid")

                    AuthenticationFailedCause.NoCredentials ->
                        call.respondRedirect("${CommonRoutes.LOGIN}?no")

                    else ->
                        call.respondRedirect(CommonRoutes.LOGIN)
                }
            }

            validate { cred: UserPasswordCredential ->
                AuthProvider.tryAuth(cred.name, cred.password)
            }
        }
    }

    Profile.register()
    District.register()
    DistrictCourierAddition.register()
    DistrictCourierDeletion.register()
    Keys.register()
    Telemetry.register()
    Supply.register()
    GoodsToSupplyAddition.register()
    GoodsFromSupplyDeletion.register()
    Report.register()
    Store.register()
    User.register()

    routing {

        route(CommonRoutes.LOGIN) { //routing
            get {
                val queryParams = call.request.queryParameters
                val message = when {
                    "invalid" in queryParams -> "Вы ввели неверное имя или пароль."
                    "no" in queryParams -> "Вам необходимо войти в систему"
                    else -> ""
                }
                call.respond(FreeMarkerContent("login.ftl", mapOf("message" to message)))
            }

            authenticate(AuthName.FORM) { //Apply auth configuration for forms
                post {
                    print("Authenticate")
                    //Principal must not be null as we are authenticated
                    val principal =
                        call.principal<TokenPrincipal>()!! // If auth configuration worked, it would have principal from AuthProvider

                    // Set the cookie to make session auth working
                    call.sessions.set(principal) // To keep the user logged it, we put his principal into session. It amkes work the second auth configuration
                    call.respondRedirect(CommonRoutes.PROFILE)
                }
            }
        }

        get(CommonRoutes.LOGOUT) {
            // Purge ExamplePrinciple from cookie data
            call.sessions.clear<TokenPrincipal>()
            call.respondRedirect(CommonRoutes.LOGIN)
        }


        authenticate(AuthName.SESSION, optional = true) {
            get("/") {
                // Redirect user to login if they're not already logged in.
                // Otherwise redirect them to a page that requires auth.
                if (call.principal<TokenPrincipal>() == null) {
                    call.respondRedirect(CommonRoutes.LOGIN)
                } else {
                    call.respondRedirect(CommonRoutes.PROFILE)
                }
            }
        }

        authenticate(AuthName.SESSION) {
            PageCollection.pages.forEach { page ->
                val url = page.getUrl()
                if (page is PageRequests) {
                    get(url) {
                        processPageResult(call, page.get(httpClient, call))
                    }
                    post(url) {
                        processPageResult(call, page.post(httpClient, call))
                    }
                } else if (page is CRUDPage) {
                    get(url) {
                        processPageResult(call, page.list(httpClient, call))
                    }
                    post(url) {
                        processPageResult(call, page.create(httpClient, call))
                    }
                    get("$url/{id}") {
                        processPageResult(call, page.read(httpClient, call))
                    }
                    post("$url/{id}") {
                        processPageResult(call, page.update(httpClient, call))
                    }
                    get("$url/{id}/delete") {
                        processPageResult(call, page.delete(httpClient, call))
                    }
                }
            }


        }


        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }



        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

