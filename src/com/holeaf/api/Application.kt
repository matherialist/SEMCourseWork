package com.holeaf.api

import com.fasterxml.jackson.databind.SerializationFeature
import com.google.gson.Gson
import com.holeaf.api.API.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject

object holeafApplication {
    lateinit var validationInterface: ValidationInterface
}

fun main(args: Array<String>): Unit = EngineMain.main(args)

val gson = Gson()

var sessions: MutableMap<Int, MutableMap<Int, MutableList<DefaultWebSocketServerSession>>> = mutableMapOf()
var eventSessions: MutableMap<Int, MutableList<DefaultWebSocketServerSession>> = mutableMapOf()

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
    val testingMode = !System.getenv("testing").isNullOrBlank()
    val myModule = if (!testing && !testingMode) org.koin.dsl.module {
        single<ValidationInterface> {
            Validation()
        }
        single<UserCommunicationInterface> {
            UserCommunication()
        }
        single<UserManagementInterface> {
            UserManagement()
        }
        single<StoreManagementInterface> {
            StoreManagement()
        }
        single<CartManagementInterface> {
            CartManagement()
        }
        single<SupplyManagementInterface> {
            SupplyManagement()
        }
        single<TelemetryManagementInterface> {
            TelemetryManagement()
        }
        single<KeyManagementInterface> {
            KeyManagement()
        }
        single<DistrictManagementInterface> {
            DistrictManagement()
        }
        single<PlaceManagementInterface> {
            PlaceManagement()
        }
        single<ChatManagementInterface> {
            ChatManagement()
        }
        single<SaleStatsManagementInterface> {
            SaleStatsManagement()
        }
        single<OrderManagementInterface> {
            OrderManagement()
        }
    } else {
        org.koin.dsl.module {
            single<ValidationInterface> {
                ValidationFake()
            }
            single<UserCommunicationInterface> {
                UserCommunicationFake()
            }
            single<UserManagementInterface> {
                UserManagementFake()
            }
            single<StoreManagementInterface> {
                StoreManagementFake()
            }
            single<CartManagementInterface> {
                CartManagementFake()
            }
            single<SupplyManagementInterface> {
                SupplyManagementFake()
            }
            single<TelemetryManagementInterface> {
                TelemetryManagementFake()
            }
            single<KeyManagementInterface> {
                KeyManagementFake()
            }
            single<DistrictManagementInterface> {
                DistrictManagementFake()
            }
            single<PlaceManagementInterface> {
                PlaceManagementFake()
            }
            single<ChatManagementInterface> {
                ChatManagementFake()
            }
            single<SaleStatsManagementInterface> {
                SaleStatsManagementFake()
            }
            single<OrderManagementInterface> {
                OrderManagementFake()
            }
        }
    }

    if (!testing && !testingMode) {
        Db.connect()
    }
    installSocket()
    install(Koin) {
        modules(myModule)
    }
    val validation by inject<ValidationInterface>()
    holeafApplication.validationInterface = validation
    installAuth(validation)
    install(ContentNegotiation) {
        gson {
        }

        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(Locations)

    val communication by inject<UserCommunicationInterface>()
    val userManagement by inject<UserManagementInterface>()
    val storeManagement by inject<StoreManagementInterface>()
    val cartManagement by inject<CartManagementInterface>()
    val supplyManagement by inject<SupplyManagementInterface>()
    val telemetryManagement by inject<TelemetryManagementInterface>()
    val districtManagement by inject<DistrictManagementInterface>()
    val keyManagement by inject<KeyManagementInterface>()
    val placeManagement by inject<PlaceManagementInterface>()
    val chatManagement by inject<ChatManagementInterface>()
    val saleStatsManagementInterface by inject<SaleStatsManagementInterface>()
    val orderManagement by inject<OrderManagementInterface>()

    routing {
        get("/ping") {
            call.respond(HttpStatusCode.OK, "It's OK.")
        }
        userRoutes(communication, userManagement)
        auth(userManagement)

        authenticate("userauth") {
            userControl(userManagement)
            profile(userManagement)
            chats(chatManagement, userManagement)
            places(userManagement, placeManagement, orderManagement)
            store(storeManagement)
            carts(cartManagement)
            supplies(supplyManagement, storeManagement)
            telemetry(telemetryManagement)
            district(districtManagement)
            keys(keyManagement, userManagement)
            salestats(saleStatsManagementInterface)
            orders(orderManagement, cartManagement)
            events(userManagement, districtManagement, orderManagement, chatManagement)
        }
    }
}