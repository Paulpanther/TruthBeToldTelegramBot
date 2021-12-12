import kotlin.random.Random
import kotlin.random.nextULong

object Lobbies {
    private val lobbies = mutableMapOf<String, Lobby>()

    fun createLobby(): Lobby {
        val lobby = Lobby(randomLobbyId())
        lobbies[lobby.id] = lobby
        return lobby
    }

    operator fun get(id: String) = lobbies[id]

    fun removeLobby(lobby: Lobby) {
        lobbies.remove(lobby.id)
    }

    private fun randomLobbyId() = Random.nextULong(0UL until 100000000000420000UL).toString(16).padStart(15, '0')
}

data class Lobby(
    val id: String,
    val users: MutableList<User> = mutableListOf(),
    var inGame: Boolean = false,
    var firstRound: Boolean = true
) {
    operator fun plusAssign(user: User) {
        user.lobbyId = id
        users += user
    }

    fun sendTranslatedMessage(key: String, vararg params: String) {
        users.sendTranslatedMessage(key, *params)
    }

    fun sendTranslatedMessage(builder: (User) -> String) {
        users.forEach { u -> u.sendTranslatedMessage(builder(u)) }
    }

    val listener get() = users.find { it.role == Role.Listener } ?: error("No Listener found")
    val explainer get() = users.find { it.role == Role.Explainer } ?: error("No Explainer found")
    val liars get() = users.filter { it.role == Role.Liar }

    val ready get() = users.all { it.ready }

    fun disband() {
        users.forEach { it.exitLobby() }
        Lobbies.removeLobby(this)
    }
}
