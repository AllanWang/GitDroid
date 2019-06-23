package ca.allanwang.gitdroid.codeview.highlighter

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import ca.allanwang.gitdroid.codeview.pattern.Decoration
import ca.allanwang.gitdroid.codeview.pattern.Lexer
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Based around https://github.com/google/code-prettify/blob/master/src/prettify.js
 */
object CodeHighlighter {

    /**
     * Base highlighting implementation.
     * Chunks the decorations into segments, and splits the work among coroutines.
     * The result is then combined together
     */
    suspend fun <T : Appendable, R : CharSequence> highlight(
        text: String,
        lexer: Lexer,
        builder: CodeHighlightBuilder<T, R>,
        chunk: Int = 100
    ): R = coroutineScope {
        val decorations: List<Decoration> = lexer.decorate(text)
        val results: List<R> = decorations.windowed(chunk, chunk - 1, true).map { decors ->
            async {
                with(builder) {
                    val b = createBuilder()
                    (0 until decors.lastIndex).forEach { i ->
                        val segment = text.substring(decors[i].pos, decors[i + 1].pos)
                        val charseq = create(decors[i].pr, segment)
                        b.append(charseq)
                    }
                    build(b)
                }
            }
        }.awaitAll()
        with(builder) {
            build(results.fold(createBuilder()) { acc, r ->
                acc.append(r)
                acc
            })
        }
    }

}

/**
 * Split [CharSequence], while retaining the [CharSequence] interface
 */
fun CharSequence.splitCharSequence(char: Char): List<CharSequence> {
    val splits: MutableList<CharSequence> = mutableListOf()
    var prev = 0
    var next = indexOf(char)
    while (next > 0) {
        splits.add(subSequence(prev, next))
        prev = next + 1
        next = indexOf(char, prev)
    }
    return splits
}

/**
 * Builder specific for Android
 * Generates a spannable string
 */
class SpannableStringHighlightBuilder(val theme: CodeTheme) :
    CodeHighlightBuilder<SpannableStringBuilder, SpannableString> {

    override fun create(pr: PR, text: String): SpannableString {
        val span = ForegroundColorSpan(theme.prColor(pr))
        val builder = SpannableStringBuilder(text)
        builder.setSpan(span, 0, builder.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        return SpannableString(builder)
    }

    override fun createBuilder(): SpannableStringBuilder = SpannableStringBuilder()

    override fun build(builder: SpannableStringBuilder): SpannableString = SpannableString(builder)
}

/**
 * Builder to create a new char sequence.
 * Chunks of content will be provided to builders, one for each coroutine.
 * The builder itself will not switch contexts, but the resulting char sequence will.
 */
interface CodeHighlightBuilder<T : Appendable, R : CharSequence> {

    fun create(pr: PR, text: String): R

    fun createBuilder(): T

    fun build(builder: T): R

}

enum class PR {
    AttrName, AttrValue, Comment, Declaration, Keyword, Literal, Nocode, Plain, Punctuation, Source, String, Tag, Type
}
