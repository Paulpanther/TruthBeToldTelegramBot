fun <T> List<T>.allButLast() = subList(0, size - 2)

fun <T> List<T>.without(vararg e: T) = filter { it !in e }
