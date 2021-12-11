object GameSystem {
    fun startGame(lobby: Lobby) {
        assignArticles(lobby)
    }

    fun chooseExplainer(msg: TMessage) {
        val user = msg.user
        val lobby = user.lobby ?: return
        if (lobby.listener != user) {
            return user.sendMessage("You have to be the Listener to choose an Explainer")
        }
        val entity = msg.entities?.firstOrNull() ?: return
        val entityText = msg.entityText(entity) ?: return
        val selected = when (entity.type) {
            TMessageEntityType.Mention -> lobby.users.find { it.chat.username == entityText }
            TMessageEntityType.TextMention -> entity.user?.let { mentioned -> lobby.users.find { it.id ==  mentioned.id } }
            else -> return
        } ?: return user.sendMessage("User is not in Lobby")

        lobby.users.without(user).sendMessage("${user.name} has chosen ${selected.name}")
        user.sendMessage("You have chosen ${selected.name}")

        if (lobby.explainer == selected) {
            lobby.sendMessage("He was the Explainer")
        } else {
            lobby.sendMessage("He was a Liar. The real explainer was ${lobby.explainer?.name ?: "Error"}")
        }

        val newPage = Wikipedia.getRandomPage() ?: return lobby.sendMessage("Could not get next Wikipedia Page")
        lobby.explainer?.ready = false
        sendWikiPage(lobby.explainer!!, newPage)
        lobby.users.without(lobby.explainer!!).sendMessage("The Explainer got a new article, please wait for them to finish reading")
    }

    fun onReady(msg: TMessage) {
        val user = msg.user
        val lobby = user.lobby ?: return
        if (user.ready) return user.sendMessage("You already are ready")
        user.ready = true

        if (lobby.firstRound && lobby.ready) {
            lobby.sendMessage("All players are ready. Assigning roles now")
            assignRandomRoles(lobby)
            startRound(lobby)
        } else if (lobby.firstRound) {
            user.sendMessage("You are ready now")
            lobby.users.without(user).sendMessage("${user.name} is ready")
        } else {
            user.sendMessage("You are ready now")
            lobby.users.without(user).sendMessage("${user.name} is ready")
            lobby.sendMessage("Starting next Round")
            startRound(lobby)
        }
    }

    private fun startRound(lobby: Lobby) {
        val explainerArticle = lobby.explainer?.articleName ?: return lobby.sendMessage("Could not get Explainer Article")
        lobby.sendMessage("The Article is: $explainerArticle")
        lobby.sendMessage("The Listener can now begin to ask questions")
        lobby.firstRound = false
    }

    private fun assignArticles(lobby: Lobby) {
        val pages = Wikipedia.getRandomPages(lobby.users.size)
        if (pages == null || pages.size != lobby.users.size) {
            return lobby.sendMessage("An Error occurred getting Wikipedia Articles")
        }

        lobby.users.zip(pages).forEach { (user, page) -> sendWikiPage(user, page) }
    }

    private fun sendWikiPage(user: User, page: WPage) {
        user.articleName = page.title
        user.sendMessage("You have been assigned the Article \"${page.title}\": ${page.url}. Please send /ready once you finished reading it")
    }

    private fun assignRandomRoles(lobby: Lobby) {
        val users = lobby.users
        if (users.size < 3) return println("Error: tried to assign roles to lobby with fewer than three users")

        val listener = users.random()
        val explainer = users.without(listener).random()
        val liars = users.without(listener, explainer)

        listener.role = Role.Listener
        explainer.role = Role.Explainer
        liars.forEach { it.role = Role.Liar }

        listener.sendMessage("You were assigned the role Listener")
        explainer.sendMessage("You were assigned the role Explainer")
        liars.sendMessage("You were assigned the role Liar")
        (liars + explainer).sendMessage("${listener.name} is the Listener")
    }
}
