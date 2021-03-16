package com.holeaf.api

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object Users : IntIdTable() {
    val username = varchar("username", 32)
    val password = varchar("password", 32)
    val email = varchar("email", 255)
    val roleId = reference("role_id", Roles)
    val photo = blob("photo").nullable()
}

object Roles : IntIdTable() {
    val name = varchar("name", 32)
    val chatEveryone = bool("chat_everyone")
    val chatCourier = bool("chat_courier")
    val chatSupplier = bool("chat_supplier")
    val chatManager = bool("chat_manager")
    val chatClient = bool("chat_client")
    val chatSeller = bool("chat_seller")
    val mapClient = bool("map_client")
    val mapSeller = bool("map_seller")
    val mapManager = bool("map_manager")
    val mapCourier = bool("map_courier")
    val storeClient = bool("store_client")
    val storeSeller = bool("store_seller")
    val supplies = bool("supplies")
    val orders = bool("orders")
    val telemetry = bool("telemetry")
    val stats = bool("stats")
    val keys = bool("keys")
    val users = bool("users")
}

object Tokens : IntIdTable() {
    val user = reference("user", Users)
    val token = varchar("token", 32)
}

object Log : IntIdTable() {
    val user = integer("user")
    val time = varchar("datetime", 32)
    val success = bool("success")
}

object ChatMessage : IntIdTable() {
    val from = reference("from", Users)
    var to = reference("to", Users)
    val time = varchar("datetime", 32)
    val content = varchar("content", 1024)
}

object Place : IntIdTable() {
    val user = reference("user", Users)
    val latitude = float("latitude")
    val longitude = float("longitude")
}

object Goods : IntIdTable() {
    val title = varchar("title", 64)
    val price = integer("price")
    val amount = integer("amount")
    val description = varchar("description", 255)
    val photo = blob("photo").nullable()
}

object Telemetry : IntIdTable() {
    val data = text("data")
}

object Districts : IntIdTable() {
    val title = varchar("title", 64)
    val longitudeTop = float("longitude_top")
    val latitudeTop = float("latitude_top")
    val longitudeBot = float("longitude_bot")
    val latitudeBot = float("latitude_bot")
}

object DistrictCouriers : IntIdTable() {
    val district = reference("district", Districts)
    val courier = reference("courier", Users)
}

object Orders : IntIdTable() {
    val client = reference("client", Users)
    val courier = reference("courier", Users).nullable()
    val latitude = float("latitude")
    val longitude = float("longitude")
    val totalCost = integer("total_cost")
    val openTime = varchar("open_time", 32)
    val closeTime = varchar("close_time", 32)
    var status = integer("status").default(0)
}

object OrderLists : IntIdTable() {
    val orderId = reference("order_id", Orders)
    val itemId = reference("item_id", Goods)
    val amount = integer("amount")
    val price = integer("price")
}

object SupplyContracts : IntIdTable() {
    val totalCost = integer("total_cost")
    val openDate = varchar("open_date", 32)
    val closeDate = varchar("close_date", 32).nullable()
    var status = integer("status").default(0)
}

object SupplyLists : IntIdTable() {
    val itemId = reference("item_id", Goods)
    val amount = integer("amount")
    val price = integer("price")
    val contractId = reference("contract_id", SupplyContracts)
}

object EncryptionKeys : IntIdTable() {
    val key = varchar("key", 16)
    val userId = reference("user_id", Users)
}

object Carts : IntIdTable() {
    val userId = reference("user_id", Users)
    val itemId = reference("item_id", Goods)
    val amount = integer("amount")
}


class Db {
    companion object {

        fun connect() {
//            print("DB=${BuildConfig.NAME}\n")

//            if (BuildConfig.)
//            when (BuildConfig.)
            Database.connect(
                 "jdbc:pgsql://77.222.60.21:15432/holeaf", driver = "com.impossibl.postgres.jdbc.PGDriver",
                 user = "holeaf", password = "holeaf"
            )
            transaction {
                SchemaUtils.create(
                    Users,
                    Roles,
                    Tokens,
                    Log,
                    ChatMessage,
                    Place,
                    Goods,
                    Telemetry,
                    Districts,
                    Orders,
                    OrderLists,
                    SupplyContracts,
                    SupplyLists,
                    EncryptionKeys,
                    Carts,
                    DistrictCouriers
                )
            }
            // val formatter = DateTimeFormatter
            //     .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
            //     .withZone(ZoneOffset.UTC)
            //     .format(Instant.now())
            // transaction {
            //     ChatMessage.insert {
            //         it[from] = 16
            //         it[to] = 18
            //         it[content] = "Hello"
            //         it[time] = formatter
            //     }
            // }
        }
    }
}
//