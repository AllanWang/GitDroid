package ca.allanwang.gitdroid.views

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.core.util.containsKey
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var data: List<ViewHolderBinding<*>> = emptyList()
        set(value) {
            field = value
            value.forEach {
                registerType(it)
            }
            notifyDataSetChanged()
        }

    private val typeCache: SparseArray<ViewHolderBinding<*>> = SparseArray()

    private fun registerType(item: ViewHolderBinding<*>) {
        if (typeCache.containsKey(item.typeId)) {
            return
        }
        typeCache.append(item.typeId, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = typeCache[viewType].onCreate(parent)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val adapter = holder.itemView.getTag(TAG_ADAPTER) as? Adapter ?: return
        val item: ViewHolderBinding<*> = adapter.data.getOrNull(position) ?: return
        item.onBind(holder, position, payloads)
        holder.itemView.setTag(TAG_ITEM, item)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, Collections.emptyList())
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        val item = holder.itemView.getTag(TAG_ITEM) as? ViewHolderBinding<*>
        item?.onRecycled(holder)
        holder.itemView.setTag(TAG_ADAPTER, null)
        holder.itemView.setTag(TAG_ITEM, null)
    }

    override fun getItemViewType(position: Int): Int {
        return data.getOrNull(position)?.typeId ?: super.getItemViewType(position)
    }

    companion object {
        private const val TAG_ADAPTER = 293
        private const val TAG_ITEM = 829

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

