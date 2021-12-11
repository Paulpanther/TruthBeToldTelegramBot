enum class Role {
    Listener,
    Liar,
    Explainer
}

object Users {
    private val users = mutableListOf<User>()
    operator fun get(chat: TChat) = users.find { it.id == chat.id } ?: User(chat).also { users += it }
}

class User(val chat: TChat) {

    var lobbyId: String? = null
    var role: Role? = null
    var ready = false
    var articleName: String? = null

    val id = chat.id

    val name by lazy {
        when {
            chat.firstName != null -> chat.firstName
            chat.username != null -> chat.username
            else -> "Anonymous User"
        }
    }

    val lobby get(): Lobby? {
        val lobbyId = lobbyId ?: return nullAndDo { sendMessage("You have to create a lobby first") }
        return Lobbies[lobbyId] ?: return nullAndDo { sendMessage("Your lobby is no longer active") }
    }

    fun sendMessage(text: String) {
        bot.sendMessage(id, text)
    }

    fun exitLobby() {
        lobbyId = null
        role = null
        ready = false
        articleName = null
    }

    override fun equals(other: Any?) = other is User && other.id == id
    override fun hashCode() = id.hashCode()
}

fun List<User>.sendMessage(text: String) {
    forEach { it.sendMessage(text) }
}

val TMessage.user get() = Users[chat]
