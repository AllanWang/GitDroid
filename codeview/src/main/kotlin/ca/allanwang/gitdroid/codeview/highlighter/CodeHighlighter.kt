package ca.allanwang.gitdroid.codeview.highlighter

import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import ca.allanwang.gitdroid.codeview.pattern.Decoration
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Based around https://github.com/google/code-prettify/blob/master/src/prettify.js
 */
object CodeHighlighter {

    /**
     * Default implementation for android spannable strings
     */
    suspend fun highlight(
        text: String,
        decorations: List<Decoration>,
        theme: CodeTheme,
        chunk: Int = 1000
    ): SpannableString =
        highlight(text, decorations, SpannableStringHighlightBuilder(theme), chunk)

    /**
     * Base highlighting implementation.
     * Chunks the decorations into segments, and splits the work among coroutines.
     * The result is then combined together
     */
    suspend fun <T : Appendable, R : CharSequence> highlight(
        text: String,
        decorations: List<Decoration>,
        builder: CodeHighlightBuilder<T, R>,
        chunk: Int = 100
    ): R = coroutineScope {
        val results: List<R> = decorations.chunked(chunk).map { decors ->
            async {
                with(builder) {
                    val b = createBuilder()
                    (0..decors.lastIndex).forEach { i ->
                        val segment = text.substring(decors[i].pos, decors[i + 1].pos)
                        val charseq = create(decors[i].pr, segment)
                        b.append(charseq)
                    }
                    val lastSegment = text.substring(decors.last().pos)
                    val lastCharSeq = create(decors.last().pr, lastSegment)
                    b.append(lastCharSeq)
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

internal class SpannableStringHighlightBuilder(val theme: CodeTheme) :
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

interface CodeHighlightBuilder<T : Appendable, R : CharSequence> {

    fun create(pr: PR, text: String): R

    fun createBuilder(): T

    fun build(builder: T): R

}

enum class PR {
    AttrName, AttrValue, Comment, Declaration, Keyword, Literal, Nocode, Plain, Punctuation, Source, String, Tag, Type
}
