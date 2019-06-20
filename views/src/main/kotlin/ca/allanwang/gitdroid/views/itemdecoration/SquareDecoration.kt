package ca.allanwang.gitdroid.views.itemdecoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.R
import ca.allanwang.kau.utils.dimenPixelSize

/**
 * Based on [DividerItemDecoration],
 * but the produced res is scaled the same along the x and y axis.
 * Depending on the space available, the icon may be smaller than sizeRes.
 */
class SquareDecoration(
    context: Context,
    private val drawable: Drawable,
    @DimenRes sizeRes: Int = R.dimen.icon_size,
    orientation: Int = LinearLayout.VERTICAL
) : RecyclerView.ItemDecoration() {

    private val size = context.dimenPixelSize(sizeRes)

    private val bounds = Rect()

    private val isHorizontal = when (orientation) {
        LinearLayout.HORIZONTAL -> true
        LinearLayout.VERTICAL -> false
        else -> throw RuntimeException("Invalid orientation flag. Must be either HORIZONTAL or VERTICAL")
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.layoutManager == null) {
            return
        }
        c.save()
        if (isHorizontal) {
            drawHorizontal(c, parent)
        } else {
            drawVertical(c, parent)
        }
        c.restore()
    }

    private fun RecyclerView.children() = (0..childCount - 2).asSequence().map { getChildAt(it) }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            c.clipRect(parent.paddingLeft, top, parent.width - parent.paddingRight, bottom)
        } else {
            top = 0
            bottom = parent.height
        }
        val trueSize = Math.min(size, bottom - top)
        val offset = (bottom - top - trueSize) / 2
        parent.children().forEach { v ->
            parent.layoutManager!!.getDecoratedBoundsWithMargins(v, bounds)
            val right = bounds.right + Math.round(v.translationX)
            val left = right - trueSize
            drawable.setBounds(left, top + offset, right, bottom - offset)
            drawable.draw(c)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            c.clipRect(left, parent.paddingTop, right, parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }
        val trueSize = Math.min(size, right - left)
        val offset = (right - left - trueSize) / 2
        parent.children().forEach { v ->
            parent.layoutManager!!.getDecoratedBoundsWithMargins(v, bounds)
            val bottom = bounds.bottom + Math.round(v.translationY)
            val top = right - trueSize
            drawable.setBounds(left + offset, top, right - offset, bottom)
            drawable.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (isHorizontal) {
            outRect.set(0, 0, size, 0)
        } else {
            outRect.set(0, 0, 0, size)
        }
    }
}