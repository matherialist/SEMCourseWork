package com.holeaf.api.API

import com.holeaf.api.*
import com.holeaf.api.model.MessageData
import com.holeaf.api.model.NewMessage
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


@Location("/messages/{from}")
data class ChatMessages(val from: Int)

data class ChatInfo(val login: String, val email: String, val id: Int, val last: String, val avatar: String, val roleId: Int)

@Location("/user/all")
class AllUsers

@Location("/user/couriers")
class AllCouriers

@Location("/user/chats")
class UserChats

interface ChatManagementInterface {
    fun getUserChats(userName: String): List<ChatInfo>
    fun getChatMessages(conversation1: Int, userName: String): List<MessageData>
    fun storeChatMessage(id: Int, toUser: Int, messageText: String, formatter: String)
}

class ChatManagement : ChatManagementInterface {
    override fun getUserChats(userName: String): List<ChatInfo> {
        return transaction {
            val currentUser = Users.select { Users.username eq userName }
            val user = currentUser.single()
            val conversation2 = user[Users.id]  // current user
            var users = ChatMessage.select { ChatMessage.to eq conversation2 }.map { it[ChatMessage.from].value }
                .toMutableList()
            users.addAll(ChatMessage.select { ChatMessage.from eq conversation2 }.map { it[ChatMessage.to].value })
            val roleId = Users.select { Users.username eq userName }.single()[Users.roleId]

            val role = Roles.select { Roles.id eq roleId }.single()
            if (role[Roles.chatEveryone]) {
                users = Users.selectAll().map { it[Users.id].value }.toMutableList()
            } else {
                if (role[Roles.chatCourier]) {
                    val courierRoles = Roles.select { Roles.mapCourier eq true }.map { it[Roles.id] }
                    users.addAll(Users.select { Users.roleId inList courierRoles }.map { it[Users.id].value })
                }

                if (role[Roles.chatSupplier]) {
                    val supplierRoles =
                        Roles.select { (Roles.mapSeller eq false) and (Roles.supplies eq true) }.map { it[Roles.id] }
                    users.addAll(Users.select { Users.roleId inList supplierRoles }.map { it[Users.id].value })
                }

                if (role[Roles.chatManager]) {
                    val managerRoles = Roles.select { Roles.mapManager eq true }.map { it[Roles.id] }
                    users.addAll(Users.select { Users.roleId inList managerRoles }.map { it[Users.id].value })
                }

                if (role[Roles.chatClient]) {
                    val clientRoles = Roles.select { Roles.mapClient eq true }.map { it[Roles.id] }
                    users.addAll(Users.select { Users.roleId inList clientRoles }.map { it[Users.id].value })
                }

                if (role[Roles.chatSeller]) {
                    val sellerRoles = Roles.select { Roles.mapSeller eq true }.map { it[Roles.id] }
                    users.addAll(Users.select { Users.roleId inList sellerRoles }.map { it[Users.id].value })
                }
            }

            Users.select { Users.id inList users }.map {
                val conversation1 = it[Users.id]
                var last = ""
                try {
                    var msg = ChatMessage.select {
                    (ChatMessage.from eq conversation1 and (ChatMessage.to eq conversation2)) or
                            (ChatMessage.to eq conversation1 and (ChatMessage.from eq conversation2))
                    }.orderBy(ChatMessage.time to SortOrder.DESC).first()
                    last = msg[ChatMessage.content]
                } catch (e: Exception) {
                    last = "Нет сообщений"
                }
                var avatar = ""
                if (it[Users.photo]!=null) {
                    avatar = Base64.getEncoder().encodeToString(it[Users.photo]?.bytes)
                }
                ChatInfo(it[Users.username], it[Users.email], it[Users.id].value, last, avatar, it[Users.roleId].value)
            }
        }
    }

    override fun getChatMessages(conversation1: Int, userName: String): List<MessageData> {
        return transaction {
            val users = Users.select { Users.username eq userName }
            try {
                val user = users.single()
                val conversation2 = user[Users.id]
                //search for messages
                ChatMessage.select {
                    ((ChatMessage.from eq conversation1) and (ChatMessage.to eq conversation2)) or
                            ((ChatMessage.to eq conversation1) and (ChatMessage.from eq conversation2))
                }.orderBy(ChatMessage.time to SortOrder.ASC).map {
                    MessageData(
                        from = it[ChatMessage.from].value,
                        to = it[ChatMessage.to].value,
                        time = it[ChatMessage.time],
                        content = it[ChatMessage.content]
                    )
                }
            } catch (e: Exception) {
                listOf()
            }
        }
    }

    override fun storeChatMessage(id: Int, toUser: Int, messageText: String, formatter: String) {
        transaction {
            ChatMessage.insert {
                it[from] = id
                it[to] = toUser
                it[content] = messageText
                it[time] = formatter
            }
        }
    }

}


fun Route.chats(chatManagementInterface: ChatManagementInterface, userManagementInterface: UserManagementInterface) {
    get<AllUsers> {
        launch {
            call.respond(userManagementInterface.getUsersDetails())
        }
    }

    get<AllCouriers> {
        launch {
            call.respond(userManagementInterface.getAllCouriers())
        }
    }

    get<UserChats> {
        holeafApplication.validationInterface.checkAccess(call, { !(it?.keys == true && !it.storeSeller) }) {
            val principal = call.principal<UserIdPrincipal>()!!
            val chats = chatManagementInterface.getUserChats(principal.name)
            launch {
                call.respond(HttpStatusCode.OK, chats)
            }
        }
    }

    get<ChatMessages> {
        val conversation1 = it.from
        val principal = call.principal<UserIdPrincipal>()!!
        launch {
            call.respond(chatManagementInterface.getChatMessages(conversation1, principal.name))
        }
    }

    webSocket("/chat") {
        val logger = call.application.environment.log
        val principal = call.principal<UserIdPrincipal>()!!
        val session = this
        var id: Int? = userManagementInterface.getUserByLogin(principal.name)?.id
        if (!sessions.keys.contains(id)) {
            sessions[id!!] = mutableMapOf()
        }
        id?.let {
            session.send(
                Frame.Text(
                    """
                           {"from": 0, "content":"connected"}
                """.trimIndent()
                )
            )
            try {
                while (true) {
                    val frame = incoming.receive()
                    if (frame is Frame.Text) {
                        val frameContent = frame.readText()
                        val decoded = gson.fromJson(frameContent, NewMessage::class.java)
                        val toUser = decoded.to
                        var messageText = decoded.content
                        if (messageText == "subscribe") {
                            var receiver = toUser
                            //register chat
                            logger.info("Subscribe to receiver: $receiver")
                            if (sessions[id]?.keys?.contains(receiver) != true) {
                                sessions[id]?.set(receiver, mutableListOf())
                                logger.info("Empty sessions for $id to $receiver")
                            }
                            logger.info("Register session for $id to $receiver: $session")
                            sessions[id]?.get(receiver)?.add(session)
                        } else {
                            //save to database
                            val formatter = DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                                .withZone(ZoneOffset.UTC)
                                .format(Instant.now())
                            chatManagementInterface.storeChatMessage(id, toUser, messageText, formatter)
                            //send to subscriptions
                            logger.info("Tracking session for accept on $toUser from $id")
                            sessions[toUser]?.get(id)?.forEach {
                                try {
                                    it.send(Frame.Text(frameContent))
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                print("Connection closed")
                e.printStackTrace()
            }
        }
    }
}

class ChatManagementFake : ChatManagementInterface {
    private var chats = mutableMapOf(
        "admin" to listOf(
            ChatInfo("admin", "12345678", 1, "test", "", 0)
        )
    )

    private var messages = mutableMapOf("admin" to mutableListOf(MessageData(1, 1, "00:00", "test")))

    override fun getUserChats(userName: String): List<ChatInfo> {
        return if (chats.containsKey(userName)) {
            chats[userName]!!
        } else {
            listOf()
        }
    }

    override fun getChatMessages(conversation1: Int, userName: String): List<MessageData> {
        return if (messages.containsKey(userName)) {
            if (messages[userName]?.find { it.from == conversation1 } != null) {
                messages[userName]!!.filter { it.from == conversation1 }
            } else {
                listOf()
            }
        } else {
            listOf()
        }
    }

    override fun storeChatMessage(id: Int, toUser: Int, messageText: String, formatter: String) {
        val userName = messages.filter { it.value.first { it.to == id }.to == id }.keys.first()
        messages[userName]?.add(MessageData(id, toUser, formatter, messageText))
    }
}

fun Application.installSocket() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(5)
        timeout = Duration.ofSeconds(60)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}
