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
import ca.allanwang.kau.utils.round
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import github.GetProfileQuery
import github.fragment.ShortRepoRowItem
import java.net.URI
import java.util.*

@BindingAdapter("languageColor")
fun TextView.languageColor(color: String) {
    val c = ColorStateList.valueOf(Color.parseColor(color))
    compoundDrawableTintList = c
    setTextColor(c)
    (this as? MaterialButton)?.iconTint = c
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
            is ShortRepoRowItem -> R.layout.view_repo to BR.repo
            else -> throw RuntimeException("Invalid pinned item type ${it.__typename}")
        }
        val binding = DataBindingUtil
            .inflate<ViewDataBinding>(inflater, layoutId, this, true)
        binding.setVariable(varId, it)
    }
}

@BindingAdapter("compactNumberText")
fun TextView.compactNumberText(count: Int) {
    fun compact(divisor: Float, suffix: Char): String {
        return "${(count / divisor).round(1)}$suffix"
    }

    val compactText = when {
        count < 1100 -> count.toString()
        count < 1e6 -> compact(1e3f, 'k')
        count < 1e9 -> compact(1e6f, 'M')
        count < 1e12 -> compact(1e9f, 'B')
        else -> count.toString()
    }

    text = compactText
}