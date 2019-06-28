package ca.allanwang.gitdroid.views.utils

import ca.allanwang.gitdroid.views.item.GenericBindingItem
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IItemAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

class FastBindingAdapter private constructor(private val adapter: ItemAdapter<GenericBindingItem>) :
    FastAdapter<GenericBindingItem>(),
    IItemAdapter<GenericBindingItem, GenericBindingItem> by adapter {

    constructor() : this(ItemAdapter())

    var lastClearTime: Long = -1

    init {
        super.addAdapter(0, adapter)
    }

    override fun clear(): FastBindingAdapter {
        if (itemCount != 0) {
            adapter.clear()
            lastClearTime = System.currentTimeMillis()
        }
        return this
    }

    override fun <A : IAdapter<GenericBindingItem>> addAdapter(index: Int, adapter: A): FastAdapter<GenericBindingItem> {
        throw IllegalArgumentException("FastBindingAdapter only allows one adapter")
    }
}