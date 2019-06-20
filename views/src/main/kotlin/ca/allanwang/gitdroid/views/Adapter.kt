package ca.allanwang.gitdroid.views

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.core.util.containsKey
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.ktx.utils.L
import kotlinx.coroutines.*
import java.util.*

typealias AdapterOnClick = (vhb: VHBindingType, view: View, info: ClickInfo) -> Boolean

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

    fun insert(data: List<VHBindingType>) {
        val newData = _data + data
        if (job?.isActive == true) {
            this.data = newData
        } else {
            this._data = newData
            notifyItemRangeInserted(newData.size - data.size, data.size)
        }
    }

    private fun <T> List<T>.subListSafe(start: Int, end: Int): List<T> =
        if (start < end) subList(Math.max(start, 0), Math.min(end, size)) else emptyList()

    fun remove(index: Int, count: Int) {
        val newData = _data.subListSafe(0, index) + _data.subListSafe(index + count, _data.size)
        _data = newData
        notifyItemRangeRemoved(index, count)
    }

    var onClick: AdapterOnClick? = null

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
        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition.takeIf { p -> p != RecyclerView.NO_POSITION } ?: return@setOnClickListener
            val vhb = it.getTag(R.id.git_view_item) as? VHBindingType ?: return@setOnClickListener
            val info = ClickInfo(position = pos, totalCount = data.size)
            if (onClick?.invoke(vhb, it, info) == true) {
                return@setOnClickListener
            }
            vhb.onClick(it, info)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val item: VHBindingType = data.getOrNull(position) ?: return
        val info = BindInfo(position = position, totalCount = data.size)
        holder.itemView.setTag(R.id.git_view_adapter, this)
        holder.itemView.setTag(R.id.git_view_item, item)
        item.onBind(holder, info, payloads)
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

        /**
         * Attaches adapter, or returns existing one if it is found
         */
        fun bind(recyclerView: RecyclerView): Adapter {
            return (recyclerView.adapter as? Adapter) ?: Adapter().also { recyclerView.adapter = it }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

