data class CommandListener(val command: String, val description: String, val action: (msg: TMessage) -> Unit)
data class ElseListener(val action: (msg: TMessage) -> Unit)

class CommandHandler(
    listeners: List<CommandListener>,
    elseListener: ElseListener
) {

    init {
        if (!Bot.setMyCommands(listeners.map { TBotCommand(it.command, it.description) })) {
            error("Could not set commands")
        }

        var nextUpdate: Long? = null
        while (true) {
            Thread.sleep(1000)

            val updates = Bot.getUpdates(nextUpdate) ?: continue
            for (update in updates) {
                val msg = update.message ?: continue
                val entity = msg.entities?.firstOrNull()

                if (entity != null && entity.type == TMessageEntityType.BotCommand) {
                    val text = msg.text ?: continue
                    val commandStr = text.substring(entity.offset, entity.offset + entity.length)
                    val command = listeners.find { "/${it.command}" == commandStr } ?: continue
                    command.action(msg)
                } else {
                    elseListener.action(msg)
                }
            }

            updates.lastOrNull()?.let { nextUpdate = it.updateId + 1 }
        }
    }
}
