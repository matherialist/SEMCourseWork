package com.holeaf.api.API

import AuthUser
import com.holeaf.api.*
import com.holeaf.api.model.Role
import com.holeaf.api.utils.passwordHash
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Location("/auth")
class Auth

data class UserModify(val id:Int, val email:String, val role:Int, val photo:String?)

fun Route.auth(userManagementInterface: UserManagementInterface) {
    post<Auth> {
        val data = call.receive<AuthUser>()
        val hashedPassword = passwordHash(data.password)
        val user = userManagementInterface.authenticate(data.login, hashedPassword)
        if (user != null) {
            userManagementInterface.registerToLog(user.id, true)
            launch {
                call.respond(user)
            }
        } else {
            print("Not authorized")
            if (userManagementInterface.searchLogin(data.login)) {
                userManagementInterface.registerToLog(userManagementInterface.getUser(data.login)?.id?:0, false)
            }
            launch {
                call.respond(HttpStatusCode.Forbidden)
            }
        }
        print(data)
    }
}

fun Route.userControl(userManagementInterface: UserManagementInterface) {
    get<EveryoneUser> {
        val principal = call.principal<UserIdPrincipal>()
        print("Everyone user called")
        print(principal.toString())

        holeafApplication.validationInterface.checkAccess(call, { it?.users == true }) {
            val result = userManagementInterface.getAllUsers()
            call.respond(HttpStatusCode.OK, result)
        }
    }

    put<UserModifyLocation> {
        holeafApplication.validationInterface.checkAccess(call, { it?.users == true }) {
            val params = call.receive<UserModify>()
            var status = userManagementInterface.modifyUser(params.id, params.email, params.role)
            if (params.photo!=null) {
                val profile = userManagementInterface.getUserById(params.id)
                status = status && userManagementInterface.setPhoto(profile?.user.orEmpty(), params.photo)
            }
            if (status) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}

fun Application.installAuth(validation: ValidationInterface) {
    install(Authentication) {
        basic("userauth") {
            realm = "Ktor Server"
            validate {
                validation.validateUser(it)
            }
        }
    }
}

interface ValidationInterface {
    fun validateUser(user: UserPasswordCredential): UserIdPrincipal?
    suspend fun checkAccess(
        call: ApplicationCall,
        check: (role: Role?) -> Boolean,
        code: suspend () -> Unit
    )
}

class Validation : ValidationInterface {
    override fun validateUser(user: UserPasswordCredential): UserIdPrincipal? {
        return if (user.name == "token") {
            //check for token
            val token = user.password
            transaction {
                try {
                    val result = Tokens.select { Tokens.token eq token }.single()
                    val usr = Users.select { Users.id eq result[Tokens.user] }.single()
                    UserIdPrincipal(usr[Users.username])
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            null
        }
    }

    override suspend fun checkAccess(call: ApplicationCall, check: (role: Role?) -> Boolean, code: suspend () -> Unit) {
        val principal = call.principal<UserIdPrincipal>()!!
        if (transaction {
                val users = Users.select { Users.username eq principal.name }
                val user = users.single()
                val roleId = user[Users.roleId].value
                val role = Roles.select { Roles.id eq roleId }.singleOrNull()
                val roleObj = role?.let {
                    Role(
                        it[Roles.id].value,
                        it[Roles.name],
                        it[Roles.chatEveryone],
                        it[Roles.chatCourier],
                        it[Roles.chatSupplier],
                        it[Roles.chatManager],
                        it[Roles.chatClient],
                        it[Roles.chatSeller],
                        it[Roles.mapClient],
                        it[Roles.mapSeller],
                        it[Roles.mapManager],
                        it[Roles.mapCourier],
                        it[Roles.storeClient],
                        it[Roles.storeSeller],
                        it[Roles.supplies],
                        it[Roles.orders],
                        it[Roles.telemetry],
                        it[Roles.stats],
                        it[Roles.keys],
                        it[Roles.users]
                    )
                }
                if (!check(roleObj)) {
                    GlobalScope.launch {
                        call.respond(HttpStatusCode.Forbidden)
                    }
                    return@transaction false
                }
                return@transaction true
            }) {
            code()
        }
    }
}

class ValidationFake : ValidationInterface {
    override fun validateUser(user: UserPasswordCredential): UserIdPrincipal? {
        return if (user.name == "token") {
            when {
                listOf(
                    "admin",
                    "seller",
                    "supplier",
                    "regulator",
                    "courier",
                    "manager",
                    "client"
                ).contains(user.password) -> UserIdPrincipal(user.password)
                else -> null
            }
        } else {
            null
        }
    }

    override suspend fun checkAccess(
        call: ApplicationCall,
        check: (role: Role?) -> Boolean,
        code: suspend () -> Unit
    ) {
        val principal = call.principal<UserIdPrincipal>()!!
        val roleObj = when (TestData.users.firstOrNull { it.username == principal.name }?.role) {
            AdminRole.id -> AdminRole
            CourierRole.id -> CourierRole
            SellerRole.id -> SellerRole
            SupplierRole.id -> SupplierRole
            RegulatorRole.id -> RegulatorRole
            ManagerRole.id -> ManagerRole
            ClientRole.id -> ClientRole
            else -> ClientRole
        }
        if (check(roleObj)) {
            code()
        } else {
            GlobalScope.launch {
                call.respond(HttpStatusCode.Forbidden, mapOf("message" to "Нет прав доступа"))
            }
        }
    }
}