package ca.allanwang.gitdroid.codeview

import android.text.SpannedString
import androidx.core.text.buildSpannedString

object Highlighter {

    fun highlight(text: String): SpannedString {
        return buildSpannedString {
            append(text)
        }
    }


}



