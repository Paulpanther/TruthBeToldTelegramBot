import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.result.Result
import com.google.gson.annotations.SerializedName

object Wikipedia {
    private val base = "https://en.wikipedia.org/w/api.php?/"

    fun getRandomPage(): WPage? {
        return getRandomPages(1)?.firstOrNull()
    }

    fun getRandomPages(amount: Int): List<WPage>? {
        val req = Fuel.get(base, listOf(
            "action" to "query",
            "generator" to "random",
            "format" to "json",
            "grnlimit" to amount,
            "prop" to "info",
            "inprop" to "url",
            "grnnamespace" to "0",
        ))
        val (_, res, result) = req.responseObject<WResponse<WPageList>>()
        val pageList = validate(res, result)
        return pageList?.pages?.values?.toList()
    }

    private fun <T> validate(res: Response, result: Result<WResponse<T>, FuelError>): T? {
        if (result is Result.Failure) {
            println("Error in Wikipedia API: Response=${res.body().asString("text/plain")}")
            return null
        }
        val data = result.get()
        return data.query
    }
}

data class WResponse<T>(val query: T)

data class WPageList(val pages: Map<String, WPage>)

data class WPage(
    @SerializedName("pageid")
    val pageId: String,
    val title: String,
    @SerializedName("fullurl")
    val url: String)
