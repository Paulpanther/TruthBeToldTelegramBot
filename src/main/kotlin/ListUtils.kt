fun <T> List<T>.allButLast() = subList(0, size - 1)

fun <T> List<T>.without(vararg e: T) = filter { it !in e }

fun <T> MutableList<T>.removeFirst(removeIf: (T) -> Boolean): T? {
    val i = indexOfFirst(removeIf)
    return if (i == -1) null else removeAt(i)
}
