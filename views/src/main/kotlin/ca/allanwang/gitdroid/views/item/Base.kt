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
import com.mikepenz.fastadapter.items.AbstractItem
import com.mikepenz.fastadapter.listeners.ClickEventHook

typealias GenericBindingItem = BindingItem<*>

abstract class BindingItem<Binding : ViewDataBinding>(open val data: Any?) : AbstractItem<BindingItem.ViewHolder>() {

    override val type: Int
        get() = layoutRes

    override fun createView(ctx: Context, parent: ViewGroup?): View {
        val binding: ViewDataBinding = DataBindingUtil.inflate(LayoutInflater.from(ctx), layoutRes, parent, false)
        return binding.root
    }

    final override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)
        val binding = DataBindingUtil.getBinding<Binding>(holder.itemView) ?: return
        binding.bindView(holder, payloads)
    }

    open fun Binding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        setVariable(BR.model, data)
    }

    final override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        val binding = DataBindingUtil.getBinding<Binding>(holder.itemView) ?: return
        binding.unbindView(holder)
        binding.unbind()
    }

    open fun Binding.unbindView(holder: ViewHolder) {}

    final override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}


abstract class BindingClickEventHook<Binding : ViewDataBinding, Item : BindingItem<Binding>> : ClickEventHook<Item>() {

    final override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
        val binding = DataBindingUtil.getBinding<Binding>(viewHolder.itemView) ?: return super.onBind(viewHolder)
        return binding.onBind(viewHolder)
    }

    open fun Binding.onBind(viewHolder: RecyclerView.ViewHolder): View? = super.onBind(viewHolder)

    final override fun onBindMany(viewHolder: RecyclerView.ViewHolder): List<View>? {
        val binding = DataBindingUtil.getBinding<Binding>(viewHolder.itemView) ?: return super.onBindMany(viewHolder)
        return binding.onBindMany(viewHolder)
    }

    open fun Binding.onBindMany(viewHolder: RecyclerView.ViewHolder): List<View>? = super.onBindMany(viewHolder)

}