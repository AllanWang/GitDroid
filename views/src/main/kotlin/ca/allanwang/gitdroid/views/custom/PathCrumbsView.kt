package ca.allanwang.gitdroid.views.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.PathCrumb
import ca.allanwang.gitdroid.views.PathCrumbVhBinding
import ca.allanwang.gitdroid.views.R
import ca.allanwang.gitdroid.views.itemdecoration.SquareDecoration
import ca.allanwang.gitdroid.views.vh
import ca.allanwang.kau.utils.drawable
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.withAlpha

typealias PathCrumbsCallback = (data: PathCrumb) -> Unit


class PathCrumbsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val adapter: ca.allanwang.gitdroid.views.Adapter = ca.allanwang.gitdroid.views.Adapter()

    var callback: PathCrumbsCallback? = null

    override fun setAdapter(adapter: Adapter<*>?) {
        throw RuntimeException("Do not set adapter; it is handled internally")
    }

    init {
        clipToPadding = false
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        super.setAdapter(adapter)
        adapter.onClick = { vhb, _, info ->
            if (vhb is PathCrumbVhBinding) {
                callback?.invoke(vhb.data)
                adapter.remove(info.position + 1, info.totalCount - info.position)
                true
            } else {
                false
            }
        }
        adapter.data = listOf(PathCrumb("/", null).vh())
        addItemDecoration(
            SquareDecoration(
                context,
                context.drawable(R.drawable.ic_chevron_right)
                    .tint(Color.WHITE.withAlpha(180)),
                R.dimen.path_crumbs_icon_size,
                LinearLayout.HORIZONTAL
            )
        )
    }

    fun addCrumb(crumb: PathCrumb) {
        adapter.insert(listOf(crumb.vh()))
        postDelayed(100) {
            smoothScrollToPosition(adapter.data.lastIndex)
        }
    }

    fun onBackPressed(): Boolean {
        val data = adapter.data
        if (data.size <= 1) {
            return false
        }
        callback?.invoke((data[data.lastIndex - 1] as PathCrumbVhBinding).data)
        adapter.remove(adapter.data.lastIndex, 1)
        return true
    }

    companion object {
        private const val SEP = '/'
    }

}
