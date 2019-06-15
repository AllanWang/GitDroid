package ca.allanwang.gitdroid.views

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

@BindingAdapter("languageColor")
fun languageColor(view: TextView, color: String) {
//    color ?: return
//    val c = Color.parseColor(color)
//    t.compoundDrawableTintList = ColorStateList.valueOf(c)
//    t.setTextColor(c)
}

@BindingAdapter("entries", "layout")
fun <T> ViewGroup.setEntries(
    entries: List<T>?, layoutId: Int
) {
    removeAllViews()
    entries ?: return
    val inflater = context.getSystemService<LayoutInflater>() ?: return
    entries.map {
        val binding = DataBindingUtil
            .inflate<ViewDataBinding>(inflater, layoutId, this, true)
//        binding.setVariable(BR.repo, it)
    }
}