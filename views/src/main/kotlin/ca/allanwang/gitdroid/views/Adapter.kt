package ca.allanwang.gitdroid.views

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.core.util.containsKey
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.util.*

class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var _data: List<VHBindingType> = emptyList()

    private var job: Job? = null

    var data: List<VHBindingType>
        get() = _data
        set(value) {
            job?.cancel()
            // TODO use better scope
            job = GlobalScope.launch {
                update(value)
            }
        }


    private suspend fun update(data: List<VHBindingType>) = withContext(Dispatchers.Main) {
        val oldData = _data
        _data = data
        data.forEach {
            registerType(it)
        }
        when {
            oldData.isEmpty() -> notifyItemRangeInserted(0, data.size)
            data.isEmpty() -> notifyItemRangeRemoved(0, oldData.size)
            else -> {
                val result: DiffUtil.DiffResult = withContext(Dispatchers.Default) {
                    DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                            oldData[oldItemPosition].isItemSame(data[newItemPosition])

                        override fun getOldListSize(): Int = oldData.size

                        override fun getNewListSize(): Int = data.size

                        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                            oldData[oldItemPosition].isContentSame(data[newItemPosition])

                        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
                            oldData[oldItemPosition].changePayload(data[newItemPosition])
                    }, true)
                }
                result.dispatchUpdatesTo(this@Adapter)
            }

        }
    }

    private val typeCache: SparseArray<VHBindingType> = SparseArray()

    private fun registerType(item: VHBindingType) {
        if (typeCache.containsKey(item.typeId)) {
            return
        }
        typeCache.append(item.typeId, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = typeCache[viewType].onCreate(parent)
        val holder = ViewHolder(view)
        holder.itemView.setTag(R.id.git_view_adapter, this)
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val adapter = holder.itemView.getTag(R.id.git_view_adapter) as? Adapter ?: return
        val item: VHBindingType = adapter.data.getOrNull(position) ?: return
        item.onBind(holder, position, payloads)
        holder.itemView.setTag(R.id.git_view_item, item)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, Collections.emptyList())
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        val item = holder.itemView.getTag(R.id.git_view_item) as? VHBindingType
        item?.onRecycled(holder)
        holder.itemView.setTag(R.id.git_view_adapter, null)
        holder.itemView.setTag(R.id.git_view_item, null)
    }

    override fun getItemViewType(position: Int): Int {
        return data.getOrNull(position)?.typeId ?: super.getItemViewType(position)
    }

    companion object {

        fun bind(
            recyclerView: RecyclerView,
            layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(recyclerView.context)
        ): Adapter {
            val adapter = Adapter()
            recyclerView.adapter = adapter
            recyclerView.layoutManager = layoutManager
            return adapter
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
