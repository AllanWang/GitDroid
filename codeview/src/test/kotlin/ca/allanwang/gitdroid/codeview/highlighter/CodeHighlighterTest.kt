package ca.allanwang.gitdroid.codeview.highlighter

import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import ca.allanwang.gitdroid.codeview.language.KotlinLang
import ca.allanwang.gitdroid.codeview.pattern.Lexer
import ca.allanwang.gitdroid.codeview.pattern.LexerOptions
import ca.allanwang.gitdroid.codeview.pattern.resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.fusesource.jansi.Ansi.ansi
import org.fusesource.jansi.AnsiConsole
import org.junit.Test

class CodeHighlighterTest {

    /**
     * See http://ascii-table.com/ansi-escape-sequences.php
     */
    object AnsiHighlightBuilder : CodeHighlightBuilder<StringBuilder, String> {

        const val ESC = "\u001B["
        const val RESET = "${ESC}0m"
        const val BOLD = "${ESC}1m"
        const val ITALIC = "${ESC}3m"
        const val UNDERSCORE = "${ESC}4m"
        const val BLACK = "${ESC}30m"
        const val RED = "${ESC}31m"
        const val GREEN = "${ESC}32m"
        const val YELLOW = "${ESC}33m"
        const val BLUE = "${ESC}34m"
        const val MAGENTA = "${ESC}35m"
        const val CYAN = "${ESC}36m"
        const val WHITE = "${ESC}37m"

        override fun create(pr: PR, text: String): String {
            val flag: String = when (pr) {
                PR.AttrName -> "$RED$BOLD"
                PR.AttrValue -> "$RED$ITALIC"
                PR.Comment -> "$GREEN$ITALIC"
                PR.Declaration -> "$CYAN$BOLD"
                PR.Keyword -> "$BLACK$BOLD"
                PR.Literal -> BLUE
                PR.Nocode -> "$BLACK$ITALIC"
                PR.Plain -> RESET
                PR.Punctuation -> MAGENTA
                PR.Source -> "$MAGENTA$BOLD"
                PR.String -> "$BLUE$BOLD"
                PR.Tag -> YELLOW
                PR.Type -> CYAN
            }
            return "$flag$text$RESET"
        }

        override fun createBuilder(): StringBuilder = StringBuilder()

        override fun build(builder: StringBuilder): String = builder.toString()

    }

    @Test
    fun ansiPreview() {

//        AnsiConsole.systemInstall()
        AnsiConsole.out.println(ansi().fgBlue().a("Jansi Hello"))
//        AnsiConsole.systemUninstall()
        with(AnsiHighlightBuilder) {
            println("Normal text")
            println("$BLACK$BOLD Black and bold")
        }

    }

    @Test
    fun kotlin() {
        highlight("Test.kt", KotlinLang)
    }

    fun highlight(path: String, lang: CodeLanguage, options: LexerOptions? = null) {
        val content = resource("source/$path")
        val lexer = Lexer(lang, options)
        val decorations = lexer.decorate(content)
        val result = runBlocking {
            withContext(Dispatchers.Default) {
                CodeHighlighter.highlight(content, decorations, AnsiHighlightBuilder)
            }
        }
        println(result)
    }

}