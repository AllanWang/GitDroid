package ca.allanwang.gitdroid.codeview.highlighter

import java.util.regex.Pattern

object PatternUtil {

    fun matchLiteral(vararg words: String) = words.joinToString("|") { Regex.escape(it) }

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