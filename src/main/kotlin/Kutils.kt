import java.io.File

fun <T> nullAndDo(action: () -> Unit): T? {
    action()
    return null
}

