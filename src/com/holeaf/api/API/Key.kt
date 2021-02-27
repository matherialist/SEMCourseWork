package com.holeaf.api.API

import UserInformation
import com.holeaf.api.EncryptionKeys
import com.holeaf.api.holeafApplication
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

interface KeyManagementInterface {
    fun getKey(user: Int): String
}

@Location("/key/{name}")
data class GetKey(val name: String)

data class KeyInformation(val key: String)

@KtorExperimentalLocationsAPI
fun Route.keys(keyManagementInterface: KeyManagementInterface, userManagementInterface: UserManagementInterface) {
    get<GetKey> { params ->
        holeafApplication.validationInterface.checkAccess(call, { it?.keys == true }) {
            val name = params.name
            var user: UserInformation? = null
            if (userManagementInterface.searchLogin(name)) {
                user = userManagementInterface.getUserByLogin(name)
            }
            if (user != null) {
                val key = keyManagementInterface.getKey(user.id)
                call.respond(KeyInformation(key))
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

}

class KeyManagement : KeyManagementInterface {
    override fun getKey(user: Int): String {
        var encryptionKey = ""
        transaction {
            val foundItem = EncryptionKeys.select { EncryptionKeys.userId eq user }
            if (foundItem.count() > 0) {
                encryptionKey = foundItem.first()[EncryptionKeys.key]
                print("Extract existing key $encryptionKey")
            } else {
                var random = UUID.randomUUID().toString()
                random = random.substring(0, random.indexOf("-"))
                print("Creating new key $random")
                EncryptionKeys.insert {
                    it[key] = random
                    it[userId] = user
                }
                encryptionKey = random
            }
        }
        return encryptionKey
    }


}

class KeyManagementFake : KeyManagementInterface {
    var key = "01234567"

    override fun getKey(user: Int): String {
        return key
    }

}