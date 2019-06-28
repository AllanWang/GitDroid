package ca.allanwang.gitdroid.views.components

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.utils.FastBindingAdapter
import ca.allanwang.gitdroid.views.utils.PathCrumb
import ca.allanwang.gitdroid.views.R
import ca.allanwang.gitdroid.views.item.PathCrumbHomeVhBinding
import ca.allanwang.gitdroid.views.item.PathCrumbVhBinding
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.gitdroid.views.itemdecoration.SquareDecoration
import ca.allanwang.kau.utils.drawable
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.withAlpha

/**
 *  Callback from crumb selection
 *  [info] is nonnull if the callback comes from a click event in the adapter.
 *  It is null otherwise, such as on back press
 */
typealias PathCrumbsCallback = (data: PathCrumb?) -> Unit


class PathCrumbsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val fastAdapter = FastBindingAdapter()

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
                        callback?.invoke(null)
                        fastAdapter
                        fastAdapter.removeRange(1, adapter.adapterItemCount - 1)
                        true
                    }
                    is PathCrumbVhBinding -> {
                        callback?.invoke(item.data)
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
        super.setAdapter(fastAdapter.apply {
            onClickListener = { v, adapter, item, position ->
                if (position == adapter.adapterItemCount - 1) {
                    false
                } else {
                    when (item) {
                        is PathCrumbHomeVhBinding -> {
                            callback?.invoke(null)
                            this@PathCrumbsView.fastAdapter.removeRange(1, adapter.adapterItemCount - 1)
                            true
                        }
                        is PathCrumbVhBinding -> {
                            callback?.invoke(item.data)
                            this@PathCrumbsView.fastAdapter.removeRange(
                                position + 1,
                                this@PathCrumbsView.fastAdapter.adapterItemCount - position
                            )
                            true
                        }
                        else -> false
                    }
                }
            }
        })
        fastAdapter.add(PathCrumbHomeVhBinding)
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

    fun setCrumbs(crumbs: List<PathCrumb>) {
        val data = listOf(PathCrumbHomeVhBinding) + crumbs.map { it.vh() }
        fastAdapter.setNewList(data)
        postDelayed(100) {
            smoothScrollToPosition(fastAdapter.adapterItemCount - 1)
        }
    }

    fun getCrumbs(): List<PathCrumb> = fastAdapter.adapterItems.mapNotNull { (it as? PathCrumbVhBinding)?.data }

    fun getCurrentCrumb(): PathCrumb? = (fastAdapter.adapterItems.last() as? PathCrumbVhBinding)?.data

    fun reset() {
        fastAdapter.removeRange(1, fastAdapter.adapterItemCount - 1)
    }

    fun onBackPressed(): Boolean {
        val size = fastAdapter.itemCount
        if (size <= 1) {
            return false
        }
        callback?.invoke((fastAdapter.getItem(size - 2) as? PathCrumbVhBinding)?.data)
        fastAdapter.remove(size - 1)
        return true
    }

}
