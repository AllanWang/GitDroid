package ca.allanwang.gitdroid.views.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.*
import ca.allanwang.gitdroid.views.itemdecoration.SquareDecoration
import ca.allanwang.kau.utils.drawable
import ca.allanwang.kau.utils.resolveColor
import ca.allanwang.kau.utils.tint

typealias PathCrumbsCallback = (String) -> Unit


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
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false).apply {
            stackFromEnd = true
        }
        super.setAdapter(adapter)
        adapter.onClick = { vhb, _, info ->
            if (vhb is PathCrumbVhBinding) {
                callback?.invoke(vhb.data.fullPath)
                adapter.remove(info.position + 1, info.totalCount - info.position)
                true
            } else {
                false
            }
        }
        adapter.data = listOf(PathCrumb("/", "/").vh()).repeat(5)
        addItemDecoration(
            SquareDecoration(
                context,
                context.drawable(R.drawable.ic_chevron_right)
                    .tint(context.resolveColor(android.R.attr.textColorSecondary)),
                R.dimen.icon_size,
                LinearLayout.HORIZONTAL
            )
        )
    }

    fun addCrumb(path: String) {
        val crumb = PathCrumb(path, "${(adapter.data.last() as PathCrumbVhBinding).data.fullPath.trim(SEP)}$path")
        adapter.insert(listOf(crumb.vh()))
        adapter.notifyItemChanged(adapter.data.size - 2) // No longer last element
    }

    companion object {
        private const val SEP = '/'
    }

}
