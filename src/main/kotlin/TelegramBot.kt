import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.result.Result
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName

object Bot {
    private val token = System.getenv("TELEGRAM_TOKEN") ?: error("TELEGRAM_TOKEN not set")
    private val base = "https://api.telegram.org/bot$token/"
    const val botName = "truth_be_told_bot"

    private val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()


    fun getChat(chatId: Long): TChat? {
        val req = Fuel.post(base + "getChat", listOf("chat_id" to chatId))
        val (_, res, result) = req.responseObject<TResponse<TChat>>(gson)
        return validate(res, result)
    }

    fun setMyCommands(commands: List<TBotCommand>): Boolean {
        val req = Fuel.post(base + "setMyCommands", listOf("commands" to commands))
        val (_, res, result) = req.responseObject<TResponse<Boolean>>(gson)
        return validate(res, result) ?: false
    }

    fun sendMessage(chatId: Long, text: String): TMessage? {
        val req = Fuel.post(base + "sendMessage", listOf("chat_id" to chatId, "text" to text))
        val (_, res, result) = req.responseObject<TResponse<TMessage>>(gson)
        return validate(res, result)
    }

    fun getUpdates(offset: Long? = null, limit: Int? = null): List<TUpdate>? {
        val req = Fuel.post(base + "getUpdates", listOf("offset" to offset, "limit" to limit))
        val (_, res, result) = req.responseObject<TResponse<List<TUpdate>>>(gson)
        return validate(res, result)
    }

    private fun <T> validate(res: Response, result: Result<TResponse<T>, FuelError>): T? {
        if (result is Result.Failure) {
            println("Error in TelegramApi: Response=${res.body().asString("text/plain")}")
            return null
        }
        val data = result.get()
        if (!data.ok) {
            println("Error in TelegramApi: Response=${data.description}")
            return null
        }
        return data.result
    }
}

data class TResponse<T>(
    val ok: Boolean,
    val result: T?,
    val description: String?
)

data class TUpdate(
    val updateId: Long,
    val message: TMessage?
)

enum class TMessageEntityType {
    @SerializedName("bot_command")
    BotCommand,
    @SerializedName("mention")
    Mention,
    @SerializedName("text_mention")
    TextMention,
}

data class TMessageEntity(
    val type: TMessageEntityType,
    val offset: Int,
    val length: Int,
    val user: TUser?
)

data class TMessage(
    val messageId: Long,
    val text: String?,
    val from: TUser?,
    val chat: TChat,
    val entities: List<TMessageEntity>?
) {
    val commandValue get(): String? {
        val command = entities?.firstOrNull() ?: return null
        val t = text ?: return null
        return t.substring(command.offset + command.length, t.length)
    }

    fun entityText(entity: TMessageEntity) = text?.substring(entity.offset, entity.offset + entity.length)
}

data class TUser(
    val id: Long,
    val firstName: String,
    val lastName: String?,
    val username: String?
)

data class TChat(
    val id: Long,
    val firstName: String?,
    val lastName: String?,
    val username: String?)

data class TBotCommand(
    val command: String,
    val description: String
)
