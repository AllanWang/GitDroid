package ca.allanwang.gitdroid.views

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.Glide
import github.GetProfileQuery

@BindingAdapter("languageColor")
fun TextView.languageColor(color: String) {
    val c = Color.parseColor(color)
    compoundDrawableTintList = ColorStateList.valueOf(c)
    setTextColor(c)
}

@BindingAdapter("glide")
fun ImageView.glide(url: Any?) {
    if (url == null) {
        Glide.with(this).clear(this)
    } else {
        Glide.with(this).load(url).into(this)
    }
}

@BindingAdapter("pinnedItems")
fun ViewGroup.pinnedItems(
    items: GetProfileQuery.PinnedItems
) {
    removeAllViews()
    val inflater = context.getSystemService<LayoutInflater>() ?: return
    items.pinnedItems?.map {
        val (layoutId, varId) = when (it) {
            is GetProfileQuery.AsRepository -> R.layout.view_pinned_repository to BR.repo
            else -> throw RuntimeException("Invalid pinned item type ${it.__typename}")
        }
        val binding = DataBindingUtil
            .inflate<ViewDataBinding>(inflater, layoutId, this, true)
        binding.setVariable(varId, it)
    }
}