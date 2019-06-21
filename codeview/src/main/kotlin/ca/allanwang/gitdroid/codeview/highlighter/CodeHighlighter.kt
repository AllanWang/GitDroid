package ca.allanwang.gitdroid.codeview.highlighter

import android.text.SpannedString
import androidx.core.text.buildSpannedString
import java.util.*
import java.util.regex.Pattern

/**
 * Based around https://github.com/google/code-prettify/blob/master/src/prettify.js
 */
object CodeHighlighter {

    fun highlight(text: String): SpannedString {
        return buildSpannedString {
            append(text)
        }
    }

//    enum class Language(val extension: String) {
//        JAVA("java"), KOTLIN("kt")
//    }

}

enum class PR {
    AttrName, AttrValue, Comment, Declaration, Keyword, Literal, Nocode, Plain, Punctuation, Source, String, Tag, Type
}

data class CodePattern(val pr: PR, val pattern: Pattern, val shortcut: String? = null)

internal data class Decoration(val pos: Int, val pr: PR)

internal data class Job(val basePos: Int, val source: String, val decorations: List<Decoration>)

inline fun <reified K : Enum<K>, V> enumMapOf(vararg pairs: Pair<K, V>): EnumMap<K, V> =
    EnumMap<K, V>(K::class.java).apply { putAll(pairs) }


data class SyntaxColors(
    val type: Int,
    val keyword: Int,
    val literal: Int,
    val comment: Int,
    val string: Int,
    val punctuation: Int,
    val plain: Int,
    val tag: Int,
    val declaration: Int,
    val attrName: Int,
    val attrValue: Int
)