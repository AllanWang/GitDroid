package ca.allanwang.gitdroid.codeview.recycler

import android.content.Context
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min

/**
 * A [LinearLayoutManager] with support for both horizontal and vertical scrolling.
 * Also prefetches items to handle smoother scrolling
 */
class CodeLayoutManager @JvmOverloads constructor(
    context: Context?,
    orientation: Int = RecyclerView.VERTICAL,
    reverseLayout: Boolean = false
) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    override fun canScrollHorizontally(): Boolean = true
    override fun canScrollVertically(): Boolean = true

    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int = maxScrollX

    private inline val maxScrollX get() = max(contentWidth - width, 0)
    private var contentWidth = 0
    private var scrollX = 0

    /**
     * Updates the max scroll value, and resets current scroll to 0.
     * Note that this should really only be done when the data has changed,
     * which is why it does nothing else to refresh the layout
     */
    fun setContentWidth(maxScroll: Int) {
        this.contentWidth = maxScroll
        this.scrollX = 0
    }

    override fun scrollHorizontallyBy(dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        if (childCount == 0 || dx == 0) {
            return 0
        }
        val trueDx = if (dx > 0) min(maxScrollX - scrollX, dx) else max(-scrollX, dx)
        scrollX += trueDx
        offsetChildrenHorizontal(-trueDx)
        return trueDx
    }

    override fun addView(child: View?, index: Int) {
        super.addView(child, index)
        if (child == null || scrollX == 0) {
            return
        }
        child.doOnNextLayout {
            it.offsetLeftAndRight(-scrollX)
        }
    }
}