package ca.allanwang.gitdroid.codeview.pattern

import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import ca.allanwang.gitdroid.codeview.language.KotlinLang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.test.Test
import kotlin.test.fail

class LexerTest {

    private object LexerListener : Lexer.Listener {
        override fun onNewDecor(token: String, pos: Int, pattern: CodePattern?, match: Array<String?>?) {
            println("New decor $pos - $token - ${pattern?.pr} -- ${pattern?.pattern}")
        }
    }

    private fun decorations(file: String, lang: CodeLanguage, listen: Boolean = true): List<Decoration> = runBlocking {
        withContext(Dispatchers.Default) {
            Lexer(lang).decorate(resource("source/$file"), LexerListener.takeIf { listen })
        }
    }

    @Test
    fun kotlin() {
        println(decorations("Test.kt", KotlinLang))
    }

}

fun resource(path: String): String =
    LexerTest::class.java.classLoader?.getResource(path)?.readText()
        ?: fail("Could not find file at ${File(path).absolutePath}")