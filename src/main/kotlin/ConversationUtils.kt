object ConversationUtils {
    fun concatenate(words: List<String>) = when (words.size) {
        0 -> ""
        1 -> words.first()
        else -> words.allButLast().joinToString(", ") + " and " + words.last()
    }
}
