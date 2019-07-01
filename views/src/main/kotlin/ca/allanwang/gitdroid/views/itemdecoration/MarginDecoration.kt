package ca.allanwang.gitdroid.views.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.logger.L

open class MarginDecoration(
    private val marginTop: Int = 0,
    private val marginBottom: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) {
            return
        }
        if (position == 0) {
            outRect.top = marginTop
            return
        }
        val adapter = parent.adapter ?: return
        if (position == adapter.itemCount - 1) {
            outRect.bottom = marginBottom
        }
    }
}