package ca.allanwang.gitdroid.codeview.pattern

import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import ca.allanwang.gitdroid.codeview.language.impl.KotlinLang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern
import kotlin.test.Test
import kotlin.test.fail

class LexerTest {

    private object LexerListener : Lexer.Listener {
        override fun onInit(tokenizer: Pattern, shortcuts: Map<Char, CodePattern>) {
            println("Using lang $tokenizer")
        }

        override fun onNewDecor(token: String, pos: Int, pattern: CodePattern?, match: Array<String?>?) {
            println("New decor $pos - $token - ${pattern?.pr} -- ${pattern?.pattern}")
        }

        override fun onEmbedded(token: String, pos: Int, pattern: CodePattern?, match: Array<String?>?) {
            println("New embed $pos - $token - ${pattern?.pr} -- ${pattern?.pattern}")
        }
    }

    private fun decorations(
        file: String,
        lang: CodeLanguage,
        listen: Boolean = System.getenv("TRAVIS") == null
    ): List<Decoration> = runBlocking {
        withContext(Dispatchers.Default) {
            Lexer(lang, LexerListener.takeIf { listen }).decorate(resource("source/$file"))
        }
    }

    @Test
    fun kotlin() {
        decorations("Test.kt", KotlinLang).forEach {
            println(it)
        }
    }

}

fun resource(path: String): String =
    LexerTest::class.java.classLoader?.getResource(path)?.readText()
        ?: fail("Could not find file at ${File(path).absolutePath}")