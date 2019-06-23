package ca.allanwang.gitdroid.codeview.highlighter

import ca.allanwang.gitdroid.codeview.language.impl.CodeLanguage
import ca.allanwang.gitdroid.codeview.language.impl.KotlinLang
import ca.allanwang.gitdroid.codeview.pattern.Lexer
import ca.allanwang.gitdroid.codeview.pattern.resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.fusesource.jansi.Ansi
import org.junit.Test
import kotlin.test.assertEquals

private fun Ansi.italic() = a(Ansi.Attribute.ITALIC)
private fun Ansi.underline() = a(Ansi.Attribute.UNDERLINE)
// Ansi.ansi() from library checks for enabled flag; we won't
private fun ansi(): Ansi = Ansi()

/**
 * Outputs ascii colored text.
 * Note that we do not use AnsiConsole since it doesn't seem to work on OSX and on Travis CI
 */
class CodeHighlighterTest {

    class AnsiAppendable : Appendable {

        val ansi = ansi()

        override fun append(csq: CharSequence?): Appendable {
            ansi.a(csq)
            return this
        }

        override fun append(csq: CharSequence?, start: Int, end: Int): Appendable {
            ansi.a(csq, start, end)
            return this
        }

        override fun append(c: Char): Appendable {
            ansi.a(c)
            return this
        }

        override fun toString(): String = ansi.toString()

        override fun hashCode(): Int = ansi.hashCode()

        override fun equals(other: Any?): Boolean = ansi == (other as? AnsiAppendable)?.ansi
    }

    /**
     * See http://ascii-table.com/ansi-escape-sequences.php
     */
    object AnsiHighlightBuilder : CodeHighlightBuilder<AnsiAppendable, String> {

        override fun create(pr: PR, text: String): String {
            return ansi().run {
                when (pr) {
                    PR.AttrName -> fgRed().bold()
                    PR.AttrValue -> fgRed()
                    PR.Comment -> fgGreen().italic()
                    PR.Declaration -> fgCyan().bold()
                    PR.Keyword -> fgBlack().bold()
                    PR.Literal -> fgBlue()
                    PR.Nocode -> fgYellow()
                    PR.Plain -> this
                    PR.Punctuation -> fgMagenta()
                    PR.Source -> fgMagenta().bold()
                    PR.String -> fgBlue().bold()
                    PR.Tag -> fgBlue().underline()
                    PR.Type -> fgCyan()
                }
            }.a(text).toString()
        }

        override fun createBuilder(): AnsiAppendable = AnsiAppendable()

        override fun build(builder: AnsiAppendable): String = builder.toString()

    }

    object NoHighlightBuilder : CodeHighlightBuilder<StringBuilder, String> {

        override fun create(pr: PR, text: String): String = text

        override fun createBuilder(): StringBuilder = StringBuilder()

        override fun build(builder: StringBuilder): String = builder.toString()

    }

    @Test
    fun ansiPreview() {
        println(
            ansi().a("Normal text").newline()
                .fgBlack().bold().a("Black and bold").newline()
                .fgCyan().italic().a("Cyan and italic").newline()
                .fgBlue().underline().a("Blue and underline")
        )
    }

    @Test
    fun kotlin() {
        highlight("Test.kt", KotlinLang)
    }

    fun highlight(path: String, lang: CodeLanguage) {
        val content = resource("source/$path")
        val (result, resultNoFormat) = runBlocking {
            withContext(Dispatchers.Default) {
                val lexer = Lexer(lang)
                val decorations = lexer.decorate(content)
                CodeHighlighter.highlight(content, decorations, AnsiHighlightBuilder) to
                        CodeHighlighter.highlight(content, decorations, NoHighlightBuilder)
            }
        }
        assertEquals(content, resultNoFormat, "Highlighting produces different content")
        println(result)
    }

}