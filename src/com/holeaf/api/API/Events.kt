package com.holeaf.api.API

import com.holeaf.api.eventSessions
import com.holeaf.api.gson
import com.holeaf.api.model.NewEvent
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun Route.events(userManagementInterface: UserManagementInterface, districtManagementInterface: DistrictManagementInterface, orderManagementInterface: OrderManagementInterface, chatManagementInterface: ChatManagementInterface) {
    webSocket("/events") {
        val logger = call.application.environment.log
        val principal = call.principal<UserIdPrincipal>()!!
        val session = this
        var id: Int? = userManagementInterface.getUserByLogin(principal.name)?.id
        if (!eventSessions.keys.contains(id)) {
            eventSessions[id!!] = mutableListOf()
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
                        val decoded = gson.fromJson(frameContent, NewEvent::class.java)
                        var messageText = decoded.content
                        logger.info("Message accepted: $messageText")
                        when {
                            messageText == "subscribe" -> {
                                //register event stream
                                logger.info("Subscribe to event receiver for id: $id, session: $session")
                                eventSessions[id]?.add(session)
                            }
                            messageText.startsWith("makeorder") -> {
                                logger.info("Make order from $id")
                                val spx = messageText.split(":")
                                val lat = spx[1].toFloatOrNull()
                                val long = spx[2].toFloatOrNull()
                                if (lat!=null && long!=null) {
                                    val district = districtManagementInterface.detectDistrict(lat, long)
                                    logger.info("Lat is $lat")
                                    logger.info("Long is $long")
                                    logger.info("District identified as $district")
                                    if (district!=null) {
                                        //отправить всем курьерам, у которых нет ни одного заказа сообщение о новом заказе
                                        val couriers = userManagementInterface.getAvailableCouriersForDistrict(district)
                                        logger.info("AllCouriers", couriers.toString())
                                        val notBusy = userManagementInterface.getCouriersWithoutOrder()
                                        logger.info("NotBusy", notBusy.toString())
                                        val subscribers = couriers.filter { courier -> notBusy.firstOrNull { it.id == courier.id }!=null }
                                        logger.info("Subscribers", subscribers.toString())
                                        val frameContent = """{"content": "order_available"}"""
                                        subscribers.forEach {
                                            logger.info("Send to subscriber: $it")
                                            eventSessions[it.id]?.forEach {
                                                logger.info("Passed through websocket session $it")
                                                try {
                                                    it.send(Frame.Text(frameContent))
                                                } catch (e:Exception) {
                                                    //just skip
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            messageText.startsWith("confirmorder") -> {
                                val spx = messageText.split(":")
                                val orderId = spx[1].toIntOrNull()
                                //получить автора заказа
                                orderId?.let {
                                    val details = orderManagementInterface.getOrderDetails(it)
                                    //добавить сообщение в чат
                                    val formatter = DateTimeFormatter
                                        .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                                        .withZone(ZoneOffset.UTC)
                                        .format(Instant.now())
                                    chatManagementInterface.storeChatMessage(id, details.clientId, "Ваш заказ в пути", formatter)
                                    eventSessions[details.clientId]?.forEach {
                                        try {
                                            it.send(Frame.Text("""{"content":"courier_assigned"}"""))
                                        } catch (e:Exception) {}
                                    }
                                }
                                logger.info("Confirm order from $id")
                            }
                            messageText.startsWith("closeorder") -> {
                                val spx = messageText.split(":")
                                val orderId = spx[1].toIntOrNull()
                                //получить автора заказа
                                orderId?.let {
                                    val details = orderManagementInterface.getOrderDetails(it)
                                    eventSessions[details.clientId]?.forEach {
                                        try {
                                            it.send(Frame.Text("order_is_closed"))
                                        } catch (e:Exception) {}
                                    }
                                }
                                logger.info("Close order from $id")
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