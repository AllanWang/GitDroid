package ca.allanwang.gitdroid.codeview.highlighter

import android.text.SpannedString
import androidx.core.text.buildSpannedString
import java.util.*

/**
 * Based around https://github.com/google/code-prettify/blob/master/src/prettify.js
 */
object CodeHighlighter {

    fun highlight(text: String): SpannedString {
        return buildSpannedString {
            append(text)
        }
    }

}

enum class PR {
    AttrName, AttrValue, Comment, Declaration, Keyword, Literal, Nocode, Plain, Punctuation, Source, String, Tag, Type
}
