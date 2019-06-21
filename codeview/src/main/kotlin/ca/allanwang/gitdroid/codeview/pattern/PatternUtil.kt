package ca.allanwang.gitdroid.codeview.pattern

import ca.allanwang.gitdroid.codeview.highlighter.CodePattern
import ca.allanwang.gitdroid.codeview.highlighter.PR
import java.util.regex.Pattern

/**
 * Note that for pattern generations, values are added verbatim.
 * Necessary escapes must be provided.
 */
object PatternUtil {

    fun matchLiteral(vararg words: String) = words.joinToString("|") { Regex.escape(it) }

    fun singleQuoted(s: String) = Pattern.compile("$s(?:[^\\\\$s]|\\\\[\\s\\S])*(?:$s|\$)")

    fun tripleQuoted(s: String) = quotedN(s, 3)

    /**
     * Matches string that starts and possibly ends (optional end) with [n] instances of [s].
     * Forbids occurrences of unescaped [s]s within the string.
     */
    fun quotedN(s: String, n: Int) = when {
        n == 1 -> singleQuoted(s)
        n > 1 -> Pattern.compile("${s.repeat(n)}(?:[^$s\\\\]|\\\\[\\s\\S]|$s{1,2}(?=[^$s]))*(?:${s.repeat(n)}|\$)")
        else -> throw RuntimeException("Cannot supply count under 1 ($n)")
    }

}

fun Pattern.fromStart() = Pattern.compile("^(?:${pattern()})")

fun Pattern.fullMatch() = Pattern.compile("^(?:${pattern()})$")

object CodePatternUtil {

    private fun combine(action: PatternUtil.() -> List<Pattern>): Pattern =
        PatternUtil.action().combine()

    // '''multi-line-string''', 'single-line-string', and double-quoted
    fun tripleQuotedStrings(vararg quotes: String): CodePattern {
        val p = combine {
            quotes.flatMap { listOf(singleQuoted(it), tripleQuoted(it)) }
        }.fromStart()
        val s = quotes.joinToString("")
        return CodePattern(PR.String, p, s)
    }

}

fun Pattern.match(input: String, isGlobal: Boolean): Array<String> {
    val result: MutableList<String> = mutableListOf()
    val matcher = matcher(input)
    while (matcher.find()) {
        result.add(matcher.group(0))
        if (!isGlobal) {
            (1..matcher.groupCount()).forEach {
                result.add(matcher.group(it))
            }
        }
    }
    return result.toTypedArray()
}