package ca.allanwang.gitdroid.data.internal

import java.io.File
import java.util.*

object PrivProps : PropReader("priv.properties", "../priv.properties") {
    val token by prop("GITHUB_TOKEN")
}

open class PropReader(vararg paths: String) {

    private val props = Properties()

    init {
        val file = paths.map(::File).firstOrNull { it.isFile }
        if (file == null) {
            println("Props not found at ${File(".").absolutePath}: $paths")
        } else {
            file.inputStream().use { props.load(it) }
        }
    }

    fun prop(name: String): Lazy<String> =
        lazy { props.getProperty(name) ?: throw RuntimeException("Property $name not found") }

    fun <T> prop(name: String, transformer: (String?) -> T): Lazy<T> =
        lazy { transformer(props.getProperty(name)) }
}