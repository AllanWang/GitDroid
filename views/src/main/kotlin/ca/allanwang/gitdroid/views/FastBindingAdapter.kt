package ca.allanwang.gitdroid.views

import ca.allanwang.gitdroid.views.item.VHBindingType
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IItemAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

class FastBindingAdapter private constructor(private val adapter: ItemAdapter<VHBindingType>) :
    FastAdapter<VHBindingType>(),
    IItemAdapter<VHBindingType, VHBindingType> by adapter {

    constructor() : this(ItemAdapter())

    init {
        super.addAdapter(0, adapter)
    }

    override fun clear(): FastBindingAdapter {
        if (itemCount != 0) {
            adapter.clear()
        }
        return this
    }

    override fun <A : IAdapter<VHBindingType>> addAdapter(index: Int, adapter: A): FastAdapter<VHBindingType> {
        throw IllegalArgumentException("FastBindingAdapter only allows one adapter")
    }
}