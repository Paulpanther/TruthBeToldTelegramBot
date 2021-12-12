enum class Role {
    Listener,
    Liar,
    Explainer
}

object Users {
    private val users = mutableListOf<User>()
    operator fun get(tUser: TUser) = users.find { it.id == tUser.id } ?: User(tUser).also { users += it }
}

class User(val tUser: TUser) {

    var lobbyId: String? = null
    var role: Role? = null
    var ready = false
    var articleName: String? = null

    val id = tUser.id

    val name  = tUser.firstName

    val lobby get(): Lobby? {
        val lobbyId = lobbyId ?: return nullAndDo { sendTranslatedMessage("errorUserNotInLobby") }
        return Lobbies[lobbyId] ?: return nullAndDo { sendTranslatedMessage("errorLobbyNotActive") }
    }

    fun sendTranslatedMessage(key: String, vararg params: String) {
        bot.sendTranslatedMessage(tUser, key, *params)
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

fun List<User>.sendTranslatedMessage(key: String, vararg params: String) {
    forEach { it.sendTranslatedMessage(key, *params) }
}

fun List<User>.sendTranslatedMessage(builder: (User) -> String) {
    forEach { u -> u.sendTranslatedMessage(builder(u)) }
}

val TMessage.user get() = Users[from ?: error("Invalid User")]
