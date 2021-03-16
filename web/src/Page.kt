package com.holeaf

import io.ktor.application.*
import io.ktor.client.*

data class ItemData(var id:Int, var title: String, var description:String, var price: Int, var amount: Int, val photo:String)

interface Page {
    fun getUrl(): String
}

interface PageRequests : Page {
    suspend fun get(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?>
    suspend fun post(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?>
}

interface CRUDPage : Page {
    //post URL
    suspend fun create(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?>

    //get URL
    suspend fun list(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?>

    //get URL/{id}
    suspend fun read(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?>

    //post URL/{id}
    suspend fun update(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?>

    //post URL/{id}/delete
    suspend fun delete(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?>
}

abstract class StubPage : PageRequests {
    override suspend fun get(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        return "notfound.ftl" to mapOf()
    }

    override suspend fun post(
        httpClient: HttpClient,
        call: ApplicationCall
    ): Pair<String, Map<String, Any?>?> {
        return "notfound.ftl" to mapOf()
    }
}

object PageCollection {

    var pages: MutableList<Page> = mutableListOf()

    fun register(page: Page) {
        pages.add(page)
    }
}



