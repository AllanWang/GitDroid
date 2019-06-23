package ca.allanwang.gitdroid.codeview.recycler

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.codeview.R
import ca.allanwang.kau.utils.dimenPixelSize

class CodeItemDecorator(
    context: Context
) : RecyclerView.ItemDecoration() {

    private val margin = context.dimenPixelSize(R.dimen.code_frame_vertical_margin)


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position == 0) {
            outRect.top = margin
            return
        }
        val adapter = parent.adapter ?: return
        if (position == adapter.itemCount - 1) {
            outRect.bottom = margin
        }
    }
}