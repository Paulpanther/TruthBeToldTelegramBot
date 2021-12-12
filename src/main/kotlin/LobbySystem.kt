object LobbySystem {
    fun createLobby(msg: TMessage) {
        val newLobby = Lobbies.createLobby()
        newLobby += msg.user
        msg.user.sendTranslatedMessage("newLobbyCreated")
        bot.sendMessage(msg.user.id, "t.me/truth_be_told_bot?start=${newLobby.id}")
    }

    fun joinLobbyOrStart(msg: TMessage) {
        val user = msg.user
        val text = msg.text ?: return user.sendTranslatedMessage("errorNoText")
        val lobbyId = text.removePrefix("/start").trim()
        if (lobbyId.isEmpty()) {
            user.sendTranslatedMessage("start", user.name)
        } else {
            val lobby = Lobbies[lobbyId] ?: return user.sendTranslatedMessage("errorNoExisitingLobby")
            if (user in lobby.users) {
                return user.sendTranslatedMessage("errorAlreadyInLobby")
            }
            lobby.sendTranslatedMessage("userJoinedLobby", user.name)
            if (lobby.users.isEmpty()) {
                user.sendTranslatedMessage("lobbyJoinSuccess")
            } else {
                val otherUsersPhrase = ConversationUtils.concatenate(user.tUser.languageCode, lobby.users.map { it.name })
                user.sendTranslatedMessage("lobbyJoinSuccessAndOthers", otherUsersPhrase)
            }
            lobby += user
        }
    }

    fun exit(msg: TMessage) {
        val user = msg.user
        val lobby = user.lobby ?: return user.sendTranslatedMessage("errorExitNoLobby")
        lobby.sendTranslatedMessage("exitLobby", user.name)
        lobby.disband()
    }

    fun startGame(msg: TMessage) {
        val user = msg.user
        val lobby = user.lobby ?: return
        if (lobby.users.firstOrNull() != user) {
            return user.sendTranslatedMessage("errorStartGameNotCreator")
        }

        if (lobby.users.size < 3) {
            return user.sendTranslatedMessage("errorToFewUsers")
        }

        lobby.inGame = true
        lobby.sendTranslatedMessage("startingGame")

        GameSystem.startGame(lobby)
    }
}
