object ConversationUtils {
    fun concatenate(lang: String, words: List<String>) = when (words.size) {
        0 -> ""
        1 -> words.first()
        else -> words.allButLast().joinToString(", ") + " ${T(lang, "and")} " + words.last()
    }
}
