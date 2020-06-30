package ca.allanwang.gitdroid.views.item

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import ca.allanwang.fastadapter.viewbinding.BindingItem
import com.bumptech.glide.Glide

typealias ViewBindingInflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

typealias ViewBindingBind<T> = (View) -> T

fun BindingItem<*>.unbindGlide(vararg imageView: ImageView) {
    if (imageView.isEmpty()) {
        return
    }
    val manager = Glide.with(imageView.first().context)
    imageView.forEach { manager.clear(it) }
}

fun BindingItem<*>.unbind(vararg imageView: ImageView) {
    imageView.forEach { it.setImageDrawable(null) }
}

fun BindingItem<*>.unbind(vararg textView: TextView) {
    textView.forEach { it.text = null }
}