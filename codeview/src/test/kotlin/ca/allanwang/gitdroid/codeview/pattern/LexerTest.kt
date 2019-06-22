package ca.allanwang.gitdroid.codeview.pattern

import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import ca.allanwang.gitdroid.codeview.language.KotlinLang
import java.io.File
import kotlin.test.Test
import kotlin.test.fail

class LexerTest {


    private fun resource(path: String): String =
        LexerTest::class.java.classLoader?.getResource(path)?.readText()
            ?: fail("Could not find file at ${File(path).absolutePath}")


    private fun decorations(file: String, lang: CodeLanguage): List<Decoration> {
        return Lexer(lang).decorate(resource("source/$file"))
    }

    @Test
    fun kotlin() {
        println(decorations("Test.kt", KotlinLang))
    }

}