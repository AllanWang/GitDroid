package ca.allanwang.gitdroid.views.item

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.BR
import com.bumptech.glide.Glide
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.items.AbstractItem

typealias VHBindingType = BindingItem<*>

abstract class BindingItem<VH : RecyclerView.ViewHolder>(open val data: Any?) : AbstractItem<VH>() {

    override val type: Int
        get() = layoutRes

    override fun createView(ctx: Context, parent: ViewGroup?): View {
        val binding: ViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(ctx), layoutRes, parent, false)
        return binding.root
    }


}

abstract class BindingViewHolder<Item : BindingItem<*>, Binding : ViewDataBinding>(itemView: View) :
    FastAdapter.ViewHolder<Item>(itemView) {

    val binding =  DataBindingUtil.getBinding<Binding>(itemView)!!

    final override fun bindView(item: Item, payloads: MutableList<Any>) {
        binding.bindView(item, payloads)
        binding.executePendingBindings()
    }

    open fun Binding.bindView(item: Item, payloads: MutableList<Any>) {
        setVariable(BR.model, item.data)
    }

    final override fun unbindView(item: Item) {
        binding.unbindView(item)
        binding.unbind()
    }

    open fun Binding.unbindView(item: Item) {}

    companion object {
        @JvmStatic
        protected fun unbindGlide(vararg imageView: ImageView) {
            if (imageView.isEmpty()) {
                return
            }
            val manager = Glide.with(imageView.first().context)
            imageView.forEach { manager.clear(it) }
        }

        @JvmStatic
        protected fun unbind(vararg imageView: ImageView) {
            imageView.forEach { it.setImageDrawable(null) }
        }

        @JvmStatic
        protected fun unbind(vararg textView: TextView) {
            textView.forEach { it.text = null }
        }
    }

}