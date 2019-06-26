package ca.allanwang.gitdroid.views

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import ca.allanwang.gitdroid.views.custom.RichTextView
import ca.allanwang.kau.utils.goneIf
import ca.allanwang.kau.utils.invisibleIf
import ca.allanwang.kau.utils.round
import ca.allanwang.kau.utils.string
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import github.type.CommentAuthorAssociation
import java.net.URI
import java.util.*

private fun notVisible(value: Any?): Boolean = when (value) {
    null -> true
    is String -> value.isBlank()
    is Boolean -> value
    is Number -> value == 0
    else -> false
}

@BindingAdapter("goneFlag")
fun View.goneFlag(value: Any?) {
    goneIf(notVisible(value))
}

@BindingAdapter("invisibleFlag")
fun View.invisibleFlag(value: Any?) {
    invisibleIf(notVisible(value))
}


@BindingAdapter("languageColor")
fun TextView.languageColor(color: String?) {
    color ?: return
    val c = ColorStateList.valueOf(Color.parseColor(color))
    compoundDrawableTintList = c
    setTextColor(c)
}

@BindingAdapter("relativeDateText")
fun TextView.relativeDateText(date: Date?) {
    if (date == null) {
        text = null
        return
    }
    text = DateUtils.getRelativeTimeSpanString(
        date.time,
        System.currentTimeMillis(),
        DateUtils.SECOND_IN_MILLIS
    )
}

private fun glideModel(model: Any?): Any? = when {
    model is URI -> model.toString()
    else -> model
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

@BindingAdapter("authorAssociation")
fun TextView.authorAssociation(association: CommentAuthorAssociation?) {
    val textRes = when (association) {
        CommentAuthorAssociation.OWNER -> R.string.author_owner
        CommentAuthorAssociation.COLLABORATOR -> R.string.author_collaborator
        CommentAuthorAssociation.CONTRIBUTOR -> R.string.author_contributor
        else -> -1
    }
    text = if (textRes != -1) context.string(textRes) else null
    goneIf(textRes == -1)

}