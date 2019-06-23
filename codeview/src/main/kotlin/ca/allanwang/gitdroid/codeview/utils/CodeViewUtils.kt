package ca.allanwang.gitdroid.codeview.utils

import android.text.StaticLayout
import android.text.TextPaint
import ca.allanwang.gitdroid.logger.L
import kotlin.math.ceil

object CodeViewUtils {

    fun computeWidth(paint: TextPaint, text: CharSequence): Float =
        StaticLayout.getDesiredWidth(text, paint)

    /**
     * Finds the max width of the provided code lines, based on the provided paint.
     * Note that this takes styling into account.
     */
    fun computeMaxWidth(paint: TextPaint, items: List<CharSequence>, candidateRatio: Float = 0.8f): Int {
        if (items.isEmpty()) return 0
        val sorted = items.sortedByDescending { it.length }
        val candidateLength: Int = (candidateRatio * sorted.first().length).toInt()
        val candidates = sorted.takeWhile { it.length > candidateLength }.toSet()
        return candidates.asSequence().map {
            val w = computeWidth(paint, it)
            w
        }.max()?.ceilInt() ?: 0
    }

}

internal fun Float.ceilInt() = ceil(this).toInt()