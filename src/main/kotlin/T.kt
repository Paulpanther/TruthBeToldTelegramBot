import com.google.gson.JsonElement
import com.google.gson.JsonParser

typealias Language = Map<String, String>

object T {
    private val languages: Map<String, Language>
    private val first: String

    init {
        val f = this::class.java.getResource("lang.json")?.openStream() ?: error("No lang.json")
        val json = JsonParser.parseReader(f.reader())
        languages = json.asMap { it.asMap { v -> v.asString } }
        first = json.asJsonObject.keySet().first()
    }

    operator fun invoke(lang: String, key: String, vararg params: String): String {
        val actualLang = languages.getOrDefault(lang, languages[first]) ?: error("Could not get Language by lang-code $lang")
        val text = actualLang.getOrDefault(key, languages[first]?.get(key)) ?: error("Could not get Translation for key $key")
        return String.format(text, *params)
    }
}

private fun <T> JsonElement.asMap(valueMapping: (JsonElement) -> T): Map<String, T> {
    val obj = asJsonObject
    return obj.keySet().associateWith { valueMapping(obj.get(it)) }
}
