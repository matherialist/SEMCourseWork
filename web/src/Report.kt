import com.holeaf.PageCollection
import com.holeaf.StubPage
import com.holeaf.authorization
import com.holeaf.prefix
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.request.*
import java.text.SimpleDateFormat

data class ProductSaleStats(val itemId: Int, val itemName:String, var totalCost: Int, var amount: Int)

data class DailySaleStats(var totalCost:Int, val date:String)

class Report : StubPage() {
    companion object {
        fun register() {
            PageCollection.register(Report())
        }
    }

    override fun getUrl(): String {
        return "/reports"
    }

    override suspend fun get(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        return "reports.ftl" to mapOf("from" to "", "to" to "", "type" to "", "data" to "")
    }

    override suspend fun post(httpClient: HttpClient, call: ApplicationCall): Pair<String, Map<String, Any?>?> {
        val req = call.receiveParameters()
        val from = req["from"].orEmpty()
        val to = req["to"].orEmpty()

        val humanFormat = SimpleDateFormat("dd.MM.yyyy")
        val isoFormat = SimpleDateFormat("yyyy-MM-dd")
        try {
            val fromIso = isoFormat.format(humanFormat.parse(from))
            val toIso = isoFormat.format(humanFormat.parse(to))

            val td = when {
                req.contains("productReport") -> "product" to httpClient.get<List<ProductSaleStats>>("$prefix/productstat/$fromIso/$toIso") {
                    authorization(call)
                }
                req.contains("dailyReport") -> "daily" to httpClient.get<List<DailySaleStats>>("$prefix/dailystat/$fromIso/$toIso") {
                    authorization(call)
                }
                else -> "" to ""
            }
            val reportParameters = mutableMapOf<String, Any>()
            reportParameters["from"] = from
            reportParameters["to"] = to
            reportParameters["type"] = td.first
            reportParameters["data"] = td.second

            return "reports.ftl" to reportParameters
        }catch (e:Exception){
            val reportParameters = mutableMapOf<String, Any>()
            reportParameters["from"] = from
            reportParameters["to"] = to
            reportParameters["type"] = ""
            reportParameters["data"] = listOf<String>()

            return "reports.ftl" to reportParameters
        }
    }

}