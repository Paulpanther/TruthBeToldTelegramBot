object LobbySystem {
    fun createLobby(msg: TMessage) {
        val newLobby = Lobbies.createLobby()
        msg.user.sendMessage("New Lobby created. Forward the following message to your friends")
        msg.user.sendMessage("t.me/truth_be_told_bot?start=${newLobby.id}")
    }

    fun joinLobbyOrStart(msg: TMessage) {
        val user = msg.user
        val text = msg.text ?: return user.sendMessage("I could not read this")
        val lobbyId = text.removePrefix("/start ")
        if (lobbyId.isEmpty()) {
            user.sendMessage("Hello ${user.name}. Send /create_lobby to create a new Lobby")
        } else {
            val lobby = Lobbies[lobbyId] ?: return user.sendMessage("You tried to join a non-existing lobby")
            if (user in lobby.users) {
                return user.sendMessage("Cannot join Lobby in which you already are")
            }
            lobby.sendMessage("User ${user.name} joined your Lobby")
            user.sendMessage("Successfully joined Lobby. Other players are ${ConversationUtils.concatenate(lobby.users.map { it.name })}")
            lobby += user
        }
    }

    fun exit(msg: TMessage) {
        val user = msg.user
        val lobby = user.lobby ?: return user.sendMessage("You have to be inside a lobby to exit one")
        lobby.sendMessage("User ${user.name} is leaving. Disbanding Lobby")
        lobby.disband()
    }

    fun startGame(msg: TMessage) {
        val user = msg.user
        val lobby = user.lobby ?: return
        if (lobby.users.firstOrNull() != user) {
            return user.sendMessage("You have to be the creator of the lobby to start a game")
        }

        lobby.inGame = true
        lobby.sendMessage("Starting Game!")

        GameSystem.startGame(lobby)
    }
}
