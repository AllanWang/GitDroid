package ca.allanwang.gitdroid.codeview.pattern

import ca.allanwang.gitdroid.codeview.highlighter.PR
import java.util.regex.Pattern

/**
 * Note that for pattern generations, values are added verbatim.
 * Necessary escapes must be provided.
 *
 * References
 * - https://github.com/google/code-prettify/blob/master/src/prettify.js
 * - https://gerrit.googlesource.com/java-prettify/+/master/src/prettify/parser/Prettify.java
 */
object PatternUtil {

    fun singleQuoted(s: String): Pattern = Pattern.compile("$s(?:[^\\\\$s]|\\\\[\\s\\S])*(?:$s|$)")

    fun singleLineQuoted(s: String): Pattern = Pattern.compile("$s(?:[^\\\\$s\r\n]|\\\\.)*(?:$s|$)")

    fun tripleQuoted(s: String): Pattern = quotedN(s, 3)

    /**
     * Matches string that starts and possibly ends (optional end) with [n] instances of [s].
     * Forbids occurrences of unescaped [s]s within the string.
     */
    fun quotedN(s: String, n: Int) = when {
        n == 1 -> singleQuoted(s)
        n > 1 -> Pattern.compile("${s.repeat(n)}(?:[^$s\\\\]|\\\\[\\s\\S]|$s{1,2}(?=[^$s]))*(?:${s.repeat(n)}|$)")
        else -> throw RuntimeException("Cannot supply count under 1 ($n)")
    }

    fun keywords(vararg key: String, blockFront: Boolean = false): Pattern =
        Pattern.compile("^${if (blockFront) "\\b" else ""}(?:${key.joinToString("|")})\\b")


    fun String.wrap(prefix: String = "", suffix: String = ""): String {
        val hasPrefix = prefix.isEmpty() || startsWith(prefix)
        val hasSuffix = suffix.isEmpty() || endsWith(suffix)
        if (hasPrefix && hasSuffix) {
            return this
        }
        val wrappedPrefix = startsWith("(")
        val wrappedSuffix = endsWith(")")
        if (startsWith("$prefix(?:") && wrappedSuffix) {
            return "$this$suffix"
        }
        if (wrappedPrefix && endsWith(")$suffix")) {
            return "$prefix$this"
        }
        val wrapped = if (wrappedPrefix && wrappedSuffix) this else "(?:$this)"
        return "$prefix$wrapped$suffix"
    }

    fun String.fromStart(): String = wrap(prefix = "^")

    fun String.fullMatch(): String = wrap(prefix = "^", suffix = "$")
}

object CodePatternUtil {

    private fun combine(action: PatternUtil.() -> List<Pattern>): Pattern =
        PatternUtil.action().combine()

    // '''multi-line-string''', 'single-line-string', and double-quoted
    fun tripleQuotedStrings(vararg quotes: String = arrayOf("'", "\\\"")): CodePattern {
        val p = combine {
            quotes.map { tripleQuoted(it) } + quotes.map { singleQuoted(it) }
        }.update { it.fromStart() }
        val s = quotes.joinToString("")
        return CodePattern(PR.String, p, s)
    }

    // 'multi-line-string', "multi-line-string", `multi-line-string`
    fun multiLineStrings(vararg quotes: String = arrayOf("'", "\\\"", "`")): CodePattern {
        val p = combine {
            quotes.map { singleQuoted(it) }
        }.update { it.fromStart() }
        val s = quotes.joinToString("")
        return CodePattern(PR.String, p, s)
    }

    // 'single-line-string', "single-line-string"
    fun singleLineStrings(vararg quotes: String = arrayOf("'", "\\\"")): CodePattern {
        val p = combine {
            quotes.map { singleLineQuoted(it) }
        }.update { it.fromStart() }
        val s = quotes.joinToString("")
        return CodePattern(PR.String, p, s)
    }

    fun keywords(vararg key: String, blockFront: Boolean = false): CodePattern {
        val p = PatternUtil.keywords(*key, blockFront = blockFront)
        return CodePattern(PR.Keyword, p)
    }

    /**
     * Typical variable name, like abc123, hello_world
     */
    fun varName(): CodePattern =
        CodePattern(PR.Plain, Pattern.compile("^[a-z_\$][a-z_\$@0-9]*", Pattern.CASE_INSENSITIVE))

    /**
     * Eg // hello
     */
    fun doubleSlashComment(): CodePattern =
        CodePattern(PR.Comment, Pattern.compile("^//[^\r\n]*"))

    /**
     * Eg /* hello */
     */
    fun slashStarCommentAndDoc(): CodePattern = CodePattern(PR.Comment, Pattern.compile("^/\\*[\\s\\S]*?(?:\\*/|\$)"))
}

fun Pattern.update(action: PatternUtil.(String) -> String): Pattern =
    PatternUtil.action(pattern()).toPattern(flags())

fun CodePattern.update(action: PatternUtil.(String) -> String): CodePattern =
    copy(pattern = pattern.update(action))

fun Pattern.match(input: String, isGlobal: Boolean): Array<String?> {
    val result: MutableList<String?> = mutableListOf()
    val matcher = matcher(input)
    while (matcher.find()) {
        result.add(matcher.group(0))
        if (!isGlobal) {
            (1 until matcher.groupCount()).forEach {
                result.add(matcher.group(it))
            }
        }
    }
    return result.toTypedArray()
}