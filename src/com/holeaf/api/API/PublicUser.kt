package com.holeaf.api.API

import NewUser
import ResetPasswordUser
import UserInformation
import com.holeaf.api.*
import com.holeaf.api.model.Role
import com.holeaf.api.utils.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.launch
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.Email
import org.apache.commons.mail.SimpleEmail
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.text.SimpleDateFormat
import java.util.*


val mail_from = "holeaf.app@gmail.com"
val mail_password = "nmspEPy0C"

data class UserInfo(val login: String, val email: String, val id: Int)
data class AuthUserInfo(val id: Int, val token: String, val role: Int, val permissions: Role)

@Location("/user/reset")
class ResetUserPassword

@Location("/roles")
class RolesLocation

@Location("/user")
class UserLocation

@Location("/user/{id}")
class UserByIdLocation(val id:Int)

@Location("/user/modify")
class UserModifyLocation

@Location("/everyone")
class EveryoneUser


fun Route.userRoutes(
    communicationInterface: UserCommunicationInterface,
    userManagementInterface: UserManagementInterface
) {
    post<UserLocation> {
        val data = call.receive<NewUser>()
        //search for same user
        var error = false
        if (data.email.isBlank() || !data.email.contains("@") || data.login.isBlank() || data.password.isBlank()) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            if (userManagementInterface.searchLogin(data.login)) {
                launch {
                    call.respond(HttpStatusCode.Conflict, mapOf("message" to "Это имя пользователя уже занято"))
                }
                error = true
            }
            if (!error) {
                if (userManagementInterface.searchEmail(data.email)) {
                    launch {
                        call.respond(
                            HttpStatusCode.Conflict,
                            mapOf("message" to "Этот адрес электронный почты уже использовался")
                        )
                    }
                    error = true
                }
            }
            if (!error) {
                userManagementInterface.registerUser(data.login, data.password, data.email)
                communicationInterface.sendEmail(
                    data.email,
                    "Регистрация в holeaf",
                    """
                                Здравствуйте, ${data.login}!

                                Вы получили это письмо, потому что зарегистрировались в системе holeaf!
                                Желаем Вам приятного времяпрепровождения в нашей системе!
                            """.trimIndent()
                )
                //return ok
                launch {
                    call.respond(mapOf("status" to "ok"))
                }
            }
        }
    }

    post<ResetUserPassword> {
        val data = call.receive<ResetPasswordUser>()
        if (data.email.isEmpty()) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            val pr = userManagementInterface.resetPassword(data.email)
            val login = pr.first
            val password = pr.second
            if (login != null && password != null) {
                //change password
                communicationInterface.sendEmail(
                    data.email,
                    "Восстановление пароля для доступа в holeaf",
                    """
                                Здравствуйте, ${login}!

                                Вы получили это письмо, потому что запросили сброс пароля в системе holeaf.
                                Ваши новые данные для входа:
                                Логин: ${login}
                                Пароль: ${password}
                            """.trimIndent()
                )
                launch {
                    call.respond(mapOf("status" to "ok"))
                }
            } else {
                launch {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }

    get<RolesLocation> {
        call.respond(HttpStatusCode.OK, userManagementInterface.getRoles())
    }
}

interface UserCommunicationInterface {
    fun sendEmail(receiver: String, subject: String, message: String)
}

class UserCommunication : UserCommunicationInterface {
    override fun sendEmail(receiver: String, subject: String, message: String) {
        print("Sending email to $receiver, subject: $subject")
        val email: Email = SimpleEmail()
        email.hostName = "smtp.gmail.com"
        email.setSmtpPort(465)
        email.setAuthenticator(DefaultAuthenticator(mail_from, mail_password))
        email.isSSLOnConnect = true
        email.setFrom(mail_from)
        email.subject = subject // subject from HTML-form
        email.setMsg(message) // message from HTML-form
        email.addTo(receiver)
        email.send() // will throw email-exception if something is wrong
    }
}

class UserCommunicationFake : UserCommunicationInterface {
    override fun sendEmail(receiver: String, subject: String, message: String) {
        print("Email sent")
    }
}

interface UserManagementInterface {
    fun authenticate(login: String, password: String): AuthUserInfo?
    fun searchLogin(login: String): Boolean
    fun searchEmail(email: String): Boolean
    fun getUsersList(): List<String>
    fun getUsersDetails(): List<UserInfo>
    fun getAllCouriers(): List<UserInfo>
    fun registerUser(login: String, password: String, email: String)
    fun getUserByLogin(login: String): UserInformation?
    fun getRoles(): List<Role>
    fun resetPassword(email: String): Pair<String?, String?>
    fun getUser(login: String): UserProfile?
    fun getUserById(id:Int): UserProfile?
    fun setPhoto(login: String, photo: String): Boolean
    fun getAllUsers(): List<UserProfile>
    fun modifyUser(id: Int, email: String, role: Int): Boolean
    fun getAvailableCouriersForDistrict(district:Int):List<UserInfo>
    fun getDistrict(id:Int): Int?
    fun getCouriersWithoutOrder(): List<UserInfo>
    fun registerToLog(user:Int, success:Boolean)
}

class UserManagement : UserManagementInterface {
    override fun authenticate(login: String, password: String): AuthUserInfo? {
        return transaction {
            val result = Users.select {
                Op.build { Users.username eq login and (Users.password eq password) }
            }
            if (result.count() > 0) {
                //generate and register random token
                val generatedToken = generateString(16)
                val authUser = result.single()
                val userId = authUser[Users.id]
                val roleId = authUser[Users.roleId].value
                Tokens.insert {
                    it[user] = userId
                    it[token] = generatedToken
                }
                val roles = getRoles()
                val permissions = roles.firstOrNull { it.id == roleId } ?: ClientRole
                AuthUserInfo(userId.value, generatedToken, roleId, permissions)
            } else {
                null
            }
        }
    }

    override fun searchLogin(login: String): Boolean {
        return transaction {
            val foundUsername = Users.select { Users.username eq login }
            return@transaction foundUsername.count() > 0
        }
    }

    override fun searchEmail(email: String): Boolean {
        return transaction {
            val foundEmail = Users.select { Users.email eq email }
            return@transaction foundEmail.count() > 0
        }
    }


    override fun getUsersList(): List<String> {
        return transaction {
            val userList = Users.selectAll().map { it[Users.username] }.toList()
            return@transaction userList
        }
    }

    override fun getUsersDetails(): List<UserInfo> {
        var result = listOf<UserInfo>()
        transaction {
            val users = Users.selectAll()
            result = users.map {
                UserInfo(it[Users.username], it[Users.email], it[Users.id].value)
            }
        }
        return result
    }

    override fun getAllCouriers(): List<UserInfo> {
        var result = listOf<UserInfo>()
        transaction {
            val roles = Roles.select { Roles.mapCourier or Roles.chatCourier }.map { it[Roles.id] }
            val users = Users.select { Users.roleId inList roles }
            result = users.map {
                UserInfo(it[Users.username], it[Users.email], it[Users.id].value)
            }
        }
        return result
    }

    override fun registerUser(login: String, password: String, email: String) {
        val hashedPassword = passwordHash(password)
        transaction {
            Users.insert {
                it[username] = login
                it[Users.password] = hashedPassword
                it[Users.email] = email
            }
        }
    }

    override fun getUserByLogin(login: String): UserInformation? {
        if (!searchLogin(login)) return null
        return transaction {
            val user = Users.select { Users.username eq login }.firstOrNull()
            if (user == null) null else UserInformation(
                user[Users.id].value,
                user[Users.username],
                user[Users.email],
                user[Users.roleId].value
            )
        }
    }

    var cachedRoles:List<Role> = mutableListOf()
    override fun getRoles(): List<Role> {
        if (cachedRoles.isEmpty())
        cachedRoles = transaction {
            Roles.selectAll().orderBy(Roles.name).map {
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
        }
        return cachedRoles
    }

    override fun resetPassword(email: String): Pair<String?, String?> {
        return transaction {
            val users = Users.select { Users.email eq email }
            try {
                val user = users.single()
                val email = user[Users.email]
                val login = user[Users.username]
                val newPassword = generateString(8)
                //change password
                Users.update({ Users.email eq email }) {
                    it[password] = passwordHash(newPassword)
                }
                login to newPassword
            } catch (e: Exception) {
                null to null
            }
        }
    }

    override fun getUser(login: String): UserProfile? {
        return transaction {
            val users = Users.select { Users.username eq login }
            try {
                val user = users.single()
                val photo = user[Users.photo]
                UserProfile(
                    user[Users.id].value, user[Users.username], user[Users.email],
                    Base64.getEncoder().encodeToString(if (photo != null) photo.bytes else byteArrayOf()),
                    user[Users.roleId].value
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun getUserById(id: Int): UserProfile? {
        return transaction {
            val users = Users.select { Users.id eq id }
            try {
                val user = users.single()
                val photo = user[Users.photo]
                UserProfile(
                    id, user[Users.username], "",
                    Base64.getEncoder().encodeToString(if (photo != null) photo.bytes else byteArrayOf()),
                    user[Users.roleId].value
                )
            } catch (e: Exception) {
                null
            }
        }

    }

    override fun setPhoto(login: String, photo: String): Boolean {
        return try {
            transaction {
                Users.update({ Users.username eq login }) {
                    it[Users.photo] = ExposedBlob(Base64.getDecoder().decode(photo))
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getAllUsers(): List<UserProfile> {
        return transaction {
            Users.selectAll().orderBy(Users.username).map { user ->
                val photo = user[Users.photo]
                val role = user[Users.roleId]
                UserProfile(
                    user[Users.id].value, user[Users.username], user[Users.email],
                    Base64.getEncoder().encodeToString(photo?.bytes ?: byteArrayOf()),
                    role.value
                )
            }
        }
    }

    override fun modifyUser(id: Int, email: String, role: Int): Boolean {
        return try {
            transaction {
                Users.update({ Users.id eq id }) {
                    it[Users.email] = email
                    it[Users.roleId] = role
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun getAvailableCouriersForDistrict(district: Int): List<UserInfo> {
        return try {
            transaction {
                val users = DistrictCouriers.select {
                    DistrictCouriers.district eq district
                }.map {
                    it[DistrictCouriers.courier].value
                }
                Users.select {
                    Users.id inList users
                }.map {
                    UserInfo(it[Users.username], it[Users.email], it[Users.id].value)
                }
            }
        } catch (e:Exception) {
            listOf()
        }
    }

    override fun getDistrict(id:Int): Int? {
        //get last location
        return try {
            transaction {
                val place = Place.select {
                    Place.user eq id
                }.firstOrNull()
                if (place != null) {
                    //определение района
                    val lat = place[Place.latitude]
                    val lng = place[Place.longitude]
                    val bounds = Districts.selectAll().filter {
                        ((lat - it[Districts.latitudeTop]) * (lat - it[Districts.latitudeBot]) < 0) &&
                                ((lng - it[Districts.longitudeTop] * (lng - it[Districts.longitudeBot]) < 0))
                    }.firstOrNull()
                    bounds?.get(Districts.id)?.value
                } else {
                    null
                }
            }
        } catch (e:Exception) {
                null
        }
    }

    override fun getCouriersWithoutOrder(): List<UserInfo> {
        return transaction {
            print("GCWO\n")
            val roles = Roles.selectAll()
            val courierRoles = roles.filter {
                it[Roles.mapCourier]
            }.map {
                it[Roles.id]
            }
            val allCouriers = Users.select { Users.roleId inList courierRoles }.map {
                it[Users.id]
            }.toMutableList()
            print("All Couriers: $allCouriers\n")
            val busyCouriers = Orders.select {
                Orders.status eq 1
            }.map {
                it[Orders.courier]
            }
            allCouriers.removeAll(busyCouriers)
            print("NotBusy Couriers: $allCouriers\n")
            Users.select { Users.id inList allCouriers }.map {
                UserInfo(it[Users.username], it[Users.email], it[Users.id].value)
            }
        }
    }

    override fun registerToLog(user: Int, success: Boolean) {
        transaction {
            Log.insert {
                val isoDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                val today = Calendar.getInstance()
                it[Log.user] = user
                it[Log.time] = isoDateFormat.format(today.time)
                it[Log.success] = success
            }
        }
    }
}

class UserManagementFake : UserManagementInterface {
    override fun authenticate(login: String, password: String): AuthUserInfo? {
        return TestData.users.filter { it.username == login }.map {
            AuthUserInfo(
                id = it.id,
                token = login,
                role = it.role,
                permissions = when (it.role) {
                    AdminRole.id -> AdminRole
                    CourierRole.id -> CourierRole
                    SellerRole.id -> SellerRole
                    SupplierRole.id -> SupplierRole
                    RegulatorRole.id -> RegulatorRole
                    ManagerRole.id -> ManagerRole
                    ClientRole.id -> ClientRole
                    else -> ClientRole
                }
            )
        }.firstOrNull()
    }

    override fun searchLogin(login: String): Boolean {
        return TestData.users.firstOrNull { it.username == login } != null
    }

    override fun searchEmail(email: String): Boolean {
        return TestData.users.firstOrNull { it.email == email } != null
    }

    override fun getUsersList(): List<String> {
        return TestData.users.map { it.username }
    }

    override fun getUsersDetails(): List<UserInfo> {
        return TestData.users.map { UserInfo(it.username, it.email, it.id) }
    }

    override fun getAllCouriers(): List<UserInfo> {
        return TestData.users.filter { it.role == CourierRole.id }.map { UserInfo(it.username, it.email, it.id) }
    }

    override fun registerUser(login: String, password: String, email: String) {
        TestData.users.add(UserInformation(TestData.users.maxOf { it.id } + 1, login, email, ClientRole.id))
    }

    override fun getUserByLogin(login: String): UserInformation? {
        return TestData.users.firstOrNull { it.username == login }

    }

    override fun getRoles(): List<Role> {
        return listOf(
            AdminRole,
            SellerRole,
            SupplierRole,
            RegulatorRole,
            CourierRole,
            ManagerRole,
            ClientRole
        )
    }

    override fun resetPassword(email: String): Pair<String?, String?> {
        val userName = TestData.users.first { it.email == email }.username
        return userName to "12345678"
    }

    override fun getUser(login: String): UserProfile? {
        return TestData.users.filter { it.username == login }.map {
            UserProfile(
                id = it.id,
                user = it.username,
                email = it.email,
                photo = "",
                role = it.role
            )
        }.firstOrNull()
    }

    override fun getUserById(id: Int): UserProfile? {
        val user = TestData.users.firstOrNull { it.id==id }
        if (user==null) return null
        return UserProfile(id=user.id, user=user.username, email="", photo = "", user.role)
    }

    override fun setPhoto(login: String, photo: String): Boolean {
        return true
    }

    override fun getAllUsers(): List<UserProfile> {
        return TestData.users.map {
            UserProfile(
                id = it.id,
                user = it.username,
                email = it.email,
                photo = "",
                role = it.role
            )
        }
    }

    override fun modifyUser(id: Int, email: String, role: Int): Boolean {
        TestData.users.first { it.id == id }.email = email
        TestData.users.first { it.id == id }.role = role
        return true
    }

    override fun getAvailableCouriersForDistrict(district: Int): List<UserInfo> {
        return mutableListOf()
    }

    override fun getDistrict(id: Int): Int? {
        return null
    }

    override fun getCouriersWithoutOrder(): List<UserInfo> {
        return listOf()
    }

    override fun registerToLog(user: Int, success: Boolean) {
    }
}