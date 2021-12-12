import io.kotest.assertions.failure
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private data class TestMessage(val chatId: Long, val text: String)

private class TestBot: AbstractBot {
    var _getChat: (chatId: Long) -> TChat? = { null }
    var _setMyCommands: (commands: List<TBotCommand>) -> Boolean = { true }
    val messages = mutableListOf<TestMessage>()
    private val updates = mutableListOf<TUpdate>()
    private var lastMessageId = 0L
    var beforeUpdate: MutableList<() -> Unit> = mutableListOf()

    override fun getChat(chatId: Long) = _getChat(chatId)
    override fun setMyCommands(commands: List<TBotCommand>) = _setMyCommands(commands)

    override fun sendTranslatedMessage(user: TUser, key: String, vararg params: String): TMessage? {
        val text = T(user.languageCode, key, *params)
        return sendMessage(user.id, text)
    }

    override fun getUpdates(offset: Long?, limit: Int?): List<TUpdate> {
        beforeUpdate.removeFirst()()

        val firstIndex = updates.indexOfFirst { it.updateId >= (offset ?: -1) }
        if (firstIndex == -1) return listOf()

        updates.removeIf { it.updateId < updates[firstIndex].updateId }
        return updates.subList(0, limit ?: updates.size)
    }

    override fun sendMessage(chatId: Long, text: String): TMessage? {
        println("Message to User $chatId: '$text'")
        messages += TestMessage(chatId, text)
        return null
    }

    fun popMessageBy(user: TUser): String? {
        return messages.removeFirst { it.chatId == user.id }?.text
    }

    fun clearMessages() {
        messages.clear()
    }

    private fun addUpdate(message: TMessage) {
        updates += TUpdate((updates.lastOrNull()?.updateId ?: -1) + 1, message)
    }

    fun addCommandMessageUpdate(user: TUser, command: String, payload: String = "") {
        println("Command from User ${user.id}: $command $payload")
        addUpdate(TMessage(++lastMessageId, "$command $payload", user, user.toChat(), listOf(TMessageEntity(TMessageEntityType.BotCommand, 0, command.length, null))))
    }

    fun addMentionUpdate(user: TUser, mentions: TUser) {
        val username = mentions.username ?: error("Could not get Username")
        addUpdate(TMessage(++lastMessageId, username, user, user.toChat(), listOf(TMessageEntity(TMessageEntityType.Mention, 0, username.length, null))))    }
}

class SystemsTest: FunSpec({
    test("It should work") {
        val testBot = TestBot()
        bot = testBot

        val u1 = TUser(0, "Tom", "Scott", "@tomscott")
        val u2 = TUser(1, "Michael", "Bay", "@michaelbay")
        val u3 = TUser(2, "Tony", "Stark", "@tonystark", "de")

        testBot._getChat = { id -> listOf(u1, u2, u3).find { it.id == id }?.toChat() }

        val commandHandler = App.registerCommands()
        commandHandler.sleepTime = 0L

        var joinId = ""
        var listener = u1
        var explainer = u2
        testBot.beforeUpdate = mutableListOf(
            {
                testBot.addCommandMessageUpdate(u1, "/start")
            }, {
                testBot.popMessageBy(u1) shouldBe "Hello Tom. Send /create_lobby to create a new Lobby"
                testBot.addCommandMessageUpdate(u1, "/create_lobby")
            }, {
                testBot.popMessageBy(u1) shouldBe "New Lobby created. Forward the following message to your friends"
                val joinLink = testBot.popMessageBy(u1) ?: throw failure("No Join Link")
                joinId = joinLink.split("=")[1]
                testBot.addCommandMessageUpdate(u2, "/start", joinId)
            }, {
                testBot.popMessageBy(u1) shouldBe "User Michael joined your Lobby"
                testBot.popMessageBy(u2) shouldBe "Successfully joined Lobby. Other players are: Tom"
                testBot.addCommandMessageUpdate(u3, "/start", joinId)
            }, {
                testBot.popMessageBy(u1) shouldBe "User Tony joined your Lobby"
                testBot.popMessageBy(u2) shouldBe "User Tony joined your Lobby"
                testBot.popMessageBy(u3) shouldBe "Lobby erfolgreich beigetreten. Die anderen Spieler sind: Tom und Michael"
                testBot.addCommandMessageUpdate(u1, "/start_game")
            }, {
                testBot.popMessageBy(u1) shouldBe "Starting Game!"
                testBot.popMessageBy(u2) shouldBe "Starting Game!"
                testBot.popMessageBy(u3) shouldBe "Spiel gestartet!"
                testBot.popMessageBy(u1)?.startsWith("You have been assigned the Article") shouldBe true
                testBot.clearMessages()
                testBot.addCommandMessageUpdate(u1, "/ready")
            }, {
                testBot.popMessageBy(u2) shouldBe "Tom is ready"
                testBot.clearMessages()
                testBot.addCommandMessageUpdate(u2, "/ready")
            }, {
                testBot.clearMessages()
                testBot.addCommandMessageUpdate(u3, "/ready")
            }, {
                testBot.popMessageBy(u2) shouldBe "All players are ready. Assigning roles now"
                val listenerMsg = testBot.messages.find { it.text.contains("role Listener") } ?: error("No Listener")
                listener = listOf(u1, u2, u3).find { it.id == listenerMsg.chatId } ?: error("Listener not in Users")
                val explainerMsg = testBot.messages.find { it.text.contains("role Explainer") } ?: error("No Explainer")
                explainer = listOf(u1, u2, u3).find { it.id == explainerMsg.chatId } ?: error("Explainer not in Users")
                testBot.addMentionUpdate(listener, explainer)
            }, {
                testBot.clearMessages()
                testBot.addCommandMessageUpdate(explainer, "/ready")
            }, {
                testBot.clearMessages()
                commandHandler.running = false
            }
        )

        commandHandler.start()
    }
})
