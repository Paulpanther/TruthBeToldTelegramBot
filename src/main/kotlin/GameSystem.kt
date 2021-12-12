object GameSystem {
    fun startGame(lobby: Lobby) {
        assignArticles(lobby)
    }

    fun chooseExplainer(msg: TMessage) {
        val user = msg.user
        val lobby = user.lobby ?: return
        if (lobby.listener != user) {
            return user.sendTranslatedMessage("errorChooseExplainerNotListener")
        }
        val entity = msg.entities?.firstOrNull() ?: return
        val entityText = msg.entityText(entity) ?: return
        val selected = when (entity.type) {
            TMessageEntityType.Mention -> lobby.users.find { it.tUser.username == entityText }
            TMessageEntityType.TextMention -> entity.user?.let { mentioned -> lobby.users.find { it.id ==  mentioned.id } }
            else -> return
        } ?: return user.sendTranslatedMessage("errorChooseExplainerNoUser")

        lobby.users.without(user).sendTranslatedMessage("chooseExplainerOthers", user.name, selected.name)
        user.sendTranslatedMessage("chooseExplainerListener", selected.name)

        if (lobby.explainer == selected) {
            lobby.sendTranslatedMessage("chooseExplainerSuccess")
        } else {
            lobby.sendTranslatedMessage("chooseExplainerFail", lobby.explainer.name)
        }

        val newPage = Wikipedia.getRandomPage() ?: return lobby.sendTranslatedMessage("errorNoWikiPage")
        lobby.explainer.ready = false
        sendWikiPage(lobby.explainer, newPage)
        lobby.users.without(lobby.explainer).sendTranslatedMessage("explainerNewArticle")
    }

    fun onReady(msg: TMessage) {
        val user = msg.user
        val lobby = user.lobby ?: return
        if (user.ready) return user.sendTranslatedMessage("errorAlreadyReady")
        user.ready = true

        if (lobby.firstRound && lobby.ready) {
            lobby.sendTranslatedMessage("allReady")
            assignRandomRoles(lobby)
            startRound(lobby)
        } else if (lobby.firstRound) {
            user.sendTranslatedMessage("userReady")
            lobby.users.without(user).sendTranslatedMessage("otherReady", user.name)
        } else {
            user.sendTranslatedMessage("userReady")
            lobby.users.without(user).sendTranslatedMessage("otherReady", user.name)
            lobby.sendTranslatedMessage("startNextRound")
            startRound(lobby)
        }
    }

    private fun startRound(lobby: Lobby) {
        val explainerArticle = lobby.explainer.articleName ?: return lobby.sendTranslatedMessage("errorExplainerArticleNull")
        lobby.sendTranslatedMessage("nextArticle", explainerArticle)
        lobby.users.without(lobby.listener).sendTranslatedMessage("beginAsking")
        lobby.listener.sendTranslatedMessage("beginAskingListener")
        lobby.firstRound = false
    }

    private fun assignArticles(lobby: Lobby) {
        val pages = Wikipedia.getRandomPages(lobby.users.size)
        if (pages == null || pages.size != lobby.users.size) {
            return lobby.sendTranslatedMessage("errorNoWikiPage")
        }

        lobby.users.zip(pages).forEach { (user, page) -> sendWikiPage(user, page) }
    }

    private fun sendWikiPage(user: User, page: WPage) {
        user.articleName = page.title
        user.sendTranslatedMessage("assignedArticle", page.title, page.url)
    }

    private fun assignRandomRoles(lobby: Lobby) {
        val users = lobby.users
        if (users.size < 3) return lobby.sendTranslatedMessage("errorRolesFewerThree")

        val listener = users.random()
        val explainer = users.without(listener).random()
        val liars = users.without(listener, explainer)

        listener.role = Role.Listener
        explainer.role = Role.Explainer
        liars.forEach { it.role = Role.Liar }

        listener.sendTranslatedMessage("userAssignedListener")
        explainer.sendTranslatedMessage("userAssignedExplainer")
        liars.sendTranslatedMessage("userAssignedLiar")
        (liars + explainer).sendTranslatedMessage("otherListener", listener.name)
    }
}
