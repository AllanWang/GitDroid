package ca.allanwang.gitdroid.views.components

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.views.R
import ca.allanwang.gitdroid.views.item.PathCrumbHomeVhBinding
import ca.allanwang.gitdroid.views.item.PathCrumbVhBinding
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.gitdroid.views.itemdecoration.SquareDecoration
import ca.allanwang.gitdroid.views.utils.PathCrumb
import ca.allanwang.kau.adapters.SingleFastAdapter
import ca.allanwang.kau.utils.drawable
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.withAlpha

/**
 *  Callback from crumb selection
 *  [info] is nonnull if the callback comes from a click event in the adapter.
 *  It is null otherwise, such as on back press
 */
typealias PathCrumbsCallback = (oid: GitObjectID?) -> Unit


class PathCrumbsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val fastAdapter = SingleFastAdapter()

    var callback: PathCrumbsCallback? = null

    override fun setAdapter(adapter: Adapter<*>?) {
        throw RuntimeException("Do not set adapter; it is handled internally")
    }

    init {
        clipToPadding = false
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        fastAdapter.onClickListener = { v, adapter, item, position ->
            if (position == adapter.adapterItemCount - 1) {
                false
            } else {
                when (item) {
                    is PathCrumbHomeVhBinding -> {
                        callback?.invoke(item.data)
                        fastAdapter.removeRange(1, adapter.adapterItemCount - 1)
                        true
                    }
                    is PathCrumbVhBinding -> {
                        callback?.invoke(item.data.oid)
                        fastAdapter.removeRange(
                            position + 1,
                            this@PathCrumbsView.fastAdapter.adapterItemCount - position
                        )
                        true
                    }
                    else -> false
                }
            }
        }
        fastAdapter.add(PathCrumbHomeVhBinding(null))
        super.setAdapter(fastAdapter)
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
        fastAdapter.add(listOf(crumb.vh()))
        postDelayed(100) {
            smoothScrollToPosition(fastAdapter.adapterItemCount - 1)
        }
    }

    fun setCrumbs(rootOid: GitObjectID?, crumbs: List<PathCrumb>) {
        val crumbsVh =
            listOf(PathCrumbHomeVhBinding(rootOid)) + crumbs.map { PathCrumbVhBinding(it) }
        fastAdapter.setWithDiff(crumbsVh, false)
        postDelayed(100)
        {
            smoothScrollToPosition(fastAdapter.adapterItemCount - 1)
        }
    }

    val crumbCount: Int
        get() = fastAdapter.adapterItemCount

    fun getCrumbs(): List<PathCrumb> =
        fastAdapter.adapterItems.mapNotNull { (it as? PathCrumbVhBinding)?.data }

    fun getCurrentCrumb(): PathCrumb? =
        (fastAdapter.adapterItems.last() as? PathCrumbVhBinding)?.data

    fun reset() {
        fastAdapter.removeRange(1, fastAdapter.adapterItemCount - 1)
    }

    fun onBackPressed(): Boolean {
        val size = fastAdapter.itemCount
        if (size <= 1) {
            return false
        }
        callback?.invoke((fastAdapter.getItem(size - 2) as? PathCrumbVhBinding)?.data?.oid)
        fastAdapter.remove(size - 1)
        return true
    }

}
