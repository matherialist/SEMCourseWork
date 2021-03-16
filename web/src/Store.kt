package com.holeaf

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.content.*
import io.ktor.request.*
import java.io.ByteArrayOutputStream
import java.util.*


class Store : CRUDPage {
    override suspend fun create(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {

        print("\nCreate\n")
        val multipart = call.receiveMultipart()
        print("\nRMD\n")
        var photo:ByteArray? = null
        var title = ""
        var description = ""
        var price = 0
        var amount = 0
        var id = 0
        print("Parsing multipart $multipart")
        multipart.forEachPart { part ->
            print(part)
            when (part) {
                is PartData.FormItem -> {
                    print("FIT ${part.name}")
                    when (part.name) {
                        "id" -> id = part.value.toInt()
                        "title" -> title = part.value
                        "description" -> description = part.value
                        "price" -> price = part.value.replace(" ","").toIntOrNull() ?: 0
                        "amount" -> amount = part.value.replace(" ","").toIntOrNull() ?: 0
                    }
                }
                is PartData.FileItem -> {
                    val bar = ByteArrayOutputStream()
                    part.streamProvider().use { input -> bar.buffered().use { output -> input.copyTo(output) } }
                    photo = bar.toByteArray()
                }
            }
            part.dispose()
        }

        print("Price int: $price\n")
        print("Amount int: $amount\n")

        val json = defaultSerializer()
        val item = ItemData(id, title, description, price, amount, Base64.getEncoder().encodeToString(photo ?: byteArrayOf()))
        httpClient.post<String>("$prefix/store") {
            authorization(call)
            body = json.write(item)
        }
        return "/store" to null
    }

    override suspend fun list(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val store = httpClient.get<List<ItemData>>("$prefix/store") {
            authorization(call)
        }
        val next = (store.maxOfOrNull { it.id } ?: 0) + 1
        return "items.ftl" to mapOf("store" to store, "next" to next)
    }

    override suspend fun read(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val id = call.parameters["id"]
        val storeItem = httpClient.get<ItemData>("$prefix/store/$id") {
            authorization(call)
        }
        print(storeItem)
        return "item.ftl" to mapOf("item" to storeItem, "message" to "")
    }

    override suspend fun update(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val id = call.parameters["id"]
        httpClient.delete<String>("$prefix/store/$id") {
            authorization(call)
        }
        return "/store" to null
    }

    override fun getUrl(): String {
        return "/store"
    }

    companion object {
        fun register() {
            PageCollection.register(Store())
        }
    }
}