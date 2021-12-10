enum class Role {
    Listener,
    Liar,
    Explainer
}

class User(val chat: TChat) {

    var lobbyId: String? = null
    var role: Role? = null
    var ready = false

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
        Bot.sendMessage(id, text)
    }

    fun exitLobby() {
        lobbyId = null
        role = null
        ready = false
    }

    override fun equals(other: Any?) = other is User && other.id == id
    override fun hashCode() = id.hashCode()
}

fun List<User>.sendMessage(text: String) {
    forEach { it.sendMessage(text) }
}

val TMessage.user get() = User(chat)
