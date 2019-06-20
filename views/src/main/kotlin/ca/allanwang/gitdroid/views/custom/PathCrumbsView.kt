package ca.allanwang.gitdroid.views.custom

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.PathCrumb
import ca.allanwang.gitdroid.views.PathCrumbVhBinding
import ca.allanwang.gitdroid.views.vh

typealias PathCrumbsCallback = (String) -> Unit


class PathCrumbsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val adapter: ca.allanwang.gitdroid.views.Adapter = ca.allanwang.gitdroid.views.Adapter()

    var callback: PathCrumbsCallback? = null

    init {
        clipToPadding = false
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        setAdapter(adapter)
        adapter.onClick = { vhb, _, info ->
            if (vhb is PathCrumbVhBinding) {
                callback?.invoke(vhb.data.fullPath)
                adapter.remove(info.position, info.totalCount - info.position)
                true
            } else {
                false
            }
        }
        adapter.data = listOf(PathCrumb("/", "/").vh())
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