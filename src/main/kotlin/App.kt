fun main() {
    CommandHandler(
        listOf(
            CommandListener("create_lobby", "Creates a new Lobby", LobbySystem::createLobby),
            CommandListener("start_game", "Starts game in the current Lobby", LobbySystem::startGame),
            CommandListener("exit", "Exists the game and leaves the Lobby", LobbySystem::exit),
            CommandListener("start", "Start Bot and join a Lobby if given", LobbySystem::joinLobbyOrStart),
            CommandListener("ready", "Notify the Bot that you finished reading your article", GameSystem::onReady)
        ),
        ElseListener(GameSystem::chooseExplainer)
    )
}
