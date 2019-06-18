package ca.allanwang.gitdroid.views

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.getSystemService
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import ca.allanwang.gitdroid.ktx.utils.L
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import github.GetProfileQuery
import java.net.URI
import java.util.*

@BindingAdapter("languageColor")
fun TextView.languageColor(color: String) {
    val c = Color.parseColor(color)
    compoundDrawableTintList = ColorStateList.valueOf(c)
    setTextColor(c)
}

private fun glideModel(model: Any?): Any? = when {
    model is URI -> model.toString()
    else -> model
}

@BindingAdapter("relativeDateText")
fun TextView.relativeDateText(date: Date) {
    text = DateUtils.getRelativeTimeSpanString(
        date.time,
        System.currentTimeMillis(),
        DateUtils.SECOND_IN_MILLIS
    )
}

@BindingAdapter("glide")
fun ImageView.glide(model: Any?) {
    if (model == null) {
        Glide.with(this).clear(this)
    } else {
        Glide.with(this)
            .load(glideModel(model))
            .into(this)
    }
}

/**
 * TODO
 *
 * Currently, multi param binding adapters don't seem to work; this is the workaround
 */
@BindingAdapter("glideRound")
fun ImageView.glideRound(model: Any?) {
    if (model == null) {
        Glide.with(this).clear(this)
    } else {
        Glide.with(this)
            .load(glideModel(model))
            .apply(RequestOptions.circleCropTransform())
            .into(this)
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