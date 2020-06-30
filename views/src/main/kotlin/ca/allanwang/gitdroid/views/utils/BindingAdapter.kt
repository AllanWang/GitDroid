package ca.allanwang.gitdroid.views.utils

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import ca.allanwang.gitdroid.data.gitNameAndOwner
import ca.allanwang.gitdroid.ktx.browser.launchUrl
import ca.allanwang.gitdroid.views.R
import ca.allanwang.kau.utils.goneIf
import ca.allanwang.kau.utils.invisibleIf
import ca.allanwang.kau.utils.round
import ca.allanwang.kau.utils.string
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import github.fragment.ShortRepoRowItem
import github.fragment.TreeEntryItem
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

fun View.goneFlag(value: Any?) {
    goneIf(notVisible(value))
}

fun View.invisibleFlag(value: Any?) {
    invisibleIf(notVisible(value))
}

fun TextView.repoHeaderText(repo: ShortRepoRowItem) {
    val ownerClick = object : ClickableSpan() {
        override fun onClick(widget: View) {
            widget.context.launchUrl(Uri.parse(repo.fragments.repoNameAndOwner.owner.url.toString()))
        }
    }
    val nao = repo.gitNameAndOwner()
    repo.fragments.repoNameAndOwner.owner.url
    movementMethod = LinkMovementMethod.getInstance()
    text = buildSpannedString {
        inSpans(ownerClick) {
            append(nao.owner)
        }
        append('/')
        append(nao.name)
    }
}

fun TextView.languageColor(color: String?) {
    color ?: return
    val c = ColorStateList.valueOf(Color.parseColor(color))
    compoundDrawableTintList = c
    setTextColor(c)
}

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

fun TextView.treeEntrySizeText(obj: TreeEntryItem?) {
    val blob = obj?.obj as? TreeEntryItem.AsBlob
    if (blob == null) {
        text = null
    } else {
        text = blob.byteSize.toString()
    }
}

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

fun TextView.memberSinceText(date: Date) {
    context.getString(R.string.member_since_s, date.toString())
}

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