import com.google.gson.Gson
import com.holeaf.*
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.request.*

data class SupplyContractItemData(val itemId:Int, var itemName:String="", val amount:Int, val price:Int, val contractId: Int)

data class SupplyList(val itemId: Int, val amount: Int, val price: Int, val contractId: Int)

data class SupplyContractData(var id: Int, var totalCost: Int, var openDate: String, var closeDate: String, var status:Int)

data class SupplyContract(val itemId:Int, val amount:Int, val price:Int)

class Supply : CRUDPage {
    override suspend fun create(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        httpClient.post<String>("$prefix/supply") {
            authorization(call)
        }
        return "/supply" to null
    }

    override suspend fun list(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val supply = httpClient.get<List<SupplyContractData>>("$prefix/supply") {
            authorization(call)
        }
        print(supply)
        val next = (supply.maxOfOrNull { it.id } ?: 0) + 1
        return "supplies.ftl" to mapOf("contracts" to supply, "next" to next)
    }

    override suspend fun read(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val id = call.parameters["id"]?.toIntOrNull() ?: 0
        val supplyContracts = httpClient.get<List<SupplyContractData>>("$prefix/supply") {
            authorization(call)
        }
        val item = supplyContracts.firstOrNull { it.id == id }
        val next = (supplyContracts.maxOfOrNull { it.id } ?: 0) + 1

        val contractItems = httpClient.get<List<SupplyContractItemData>>("$prefix/supply/$id") {
            authorization(call)
        }
//        val contractItems = listOf<SupplyContractItemData>()

//        val items = listOf<ItemData>()
        val items = httpClient.get<List<ItemData>>("$prefix/store") {
            authorization(call)
        }

        contractItems.forEach {
            ci ->
            print("Search for ${ci.itemId} in ${items}")
            items.firstOrNull { it.id == ci.itemId }?.let {
                ci.itemName = it.title
            }
        }

        return "supply.ftl" to mapOf(
            "contracts" to supplyContracts,
            "contract" to item,
            "items" to contractItems,
            "allitems" to items,
            "next" to next,
            "message" to ""
        )
    }

    override suspend fun update(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        print("\nUpdating...")
        val params = call.receiveParameters()
        val id = params["id"]
        val status = when {
            params.contains("create") -> 1
            params.contains("accept") -> 2
            params.contains("reject") -> 3
            else -> 0
        }
        httpClient.get<String>("$prefix/supply/status/$id/$status") {
            authorization(call)
        }
        return "/supply" to null
    }

    override suspend fun delete(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val id = call.parameters["id"]
        httpClient.delete<String>("$prefix/supply/$id") {
            authorization(call)
        }
        return "/supply" to null
    }

    override fun getUrl(): String {
        return "/supply"
    }

    companion object {
        fun register() {
            PageCollection.register(Supply())
        }
    }
}

class GoodsToSupplyAddition : StubPage() {
    override fun getUrl() = "/supply/{id}/item"

    override suspend fun post(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val json = defaultSerializer()
        val data = call.receiveParameters()
        val id = data["id"]?.toIntOrNull() ?: 0
        val item = data["item"]?.toIntOrNull() ?: 0
        val price = data["price"]?.toIntOrNull() ?: 0
        val amount = data["amount"]?.toIntOrNull() ?: 0
        httpClient.post<String>("$prefix/supply/$id/$item") {
            authorization(call)
            body = json.write(SupplyContract(item, amount, price))
        }
        return "/supply/$id" to null
    }

    companion object {
        fun register() {
            PageCollection.register(GoodsToSupplyAddition())
        }
    }
}

class GoodsFromSupplyDeletion : StubPage() {
    override fun getUrl() = "/supply/{id}/{item}/delete"

    override suspend fun get(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val id = call.parameters["id"]
        val item = call.parameters["item"]
        print("Det")
        httpClient.delete<String>("$prefix/supply/$id/$item") {
            authorization(call)
        }
        return "/supply/$id" to null
    }

    companion object {
        fun register() {
            PageCollection.register(GoodsFromSupplyDeletion())
        }
    }
}
