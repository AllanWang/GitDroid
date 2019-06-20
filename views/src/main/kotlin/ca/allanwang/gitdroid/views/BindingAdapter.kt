package ca.allanwang.gitdroid.views

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.goneIf
import ca.allanwang.kau.utils.round
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import github.fragment.TreeEntryItem
import java.net.URI
import java.util.*

@BindingAdapter("goneFlag")
fun View.goneFlag(value: Any?) {
    when (value) {
        null -> gone()
        is String -> goneIf(value.isBlank())
        is Boolean -> goneIf(value)
        is Number -> goneIf(value == 0)
        // Else keep as is
    }
}

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

@BindingAdapter("android:src")
fun ImageView.setImageViewResource(resource: Int) {
    setImageResource(resource)
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

@BindingAdapter("memberSinceText")
fun TextView.memberSinceText(date: Date) {
    context.getString(R.string.member_since_s, date.toString())
}