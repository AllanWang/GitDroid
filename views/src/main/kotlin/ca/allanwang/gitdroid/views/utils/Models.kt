package ca.allanwang.gitdroid.views.utils

import android.os.Parcelable
import android.view.View
import androidx.annotation.DrawableRes
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.GitRefs
import ca.allanwang.gitdroid.data.gitNameAndOwner
import ca.allanwang.gitdroid.views.R
import github.fragment.ShortIssueRowItem
import github.fragment.ShortPullRequestRowItem
import github.fragment.ShortRef
import kotlinx.android.parcel.Parcelize
import java.net.URI
import java.util.*

data class GitIssueOrPr(
    val id: String,
    val databaseId: Int?,
    val url: URI,
    val avatarUrl: URI?,
    val login: String?,
    val createdAt: Date,
    val number: Int,
    val title: String,
    val nameAndOwner: GitNameAndOwner,
    val commentCount: Int,
    val locked: Boolean
) {
    val nameWithOwner: String
        get() = nameAndOwner.nameWithOwner

}

fun ShortIssueRowItem.issueOrPr(): GitIssueOrPr =
    GitIssueOrPr(
        id = id,
        databaseId = databaseId,
        url = url,
        avatarUrl = author?.fragments?.shortActor?.avatarUrl,
        login = author?.fragments?.shortActor?.login,
        createdAt = createdAt,
        number = number,
        title = title,
        nameAndOwner = gitNameAndOwner(),
        commentCount = comments.totalCount,
        locked = isLocked
    )

fun ShortPullRequestRowItem.issueOrPr(): GitIssueOrPr =
    GitIssueOrPr(
        id = id,
        databaseId = databaseId,
        url = url,
        avatarUrl = author?.fragments?.shortActor?.avatarUrl,
        login = author?.fragments?.shortActor?.login,
        createdAt = createdAt,
        number = number,
        title = title,
        nameAndOwner = gitNameAndOwner(),
        commentCount = comments.totalCount,
        locked = isLocked
    )

data class SlimEntry(@DrawableRes val icon: Int, val text: String, val onClick: ((View) -> Unit)? = null)

@Parcelize
data class PathCrumb(val segment: String, val oid: GitObjectID) : Parcelable

data class RefEntry(@DrawableRes val icon: Int, val ref: ShortRef, val current: Boolean)

fun GitRefs.entries(selected: GitObjectID? = null): List<RefEntry> =
    branchRefs.map { ref ->
        RefEntry(
            R.drawable.ic_branch,
            ref,
            ref.target.oid == selected
        )
    } +
            tagRefs.map { ref ->
                RefEntry(
                    R.drawable.ic_label,
                    ref,
                    ref.target.oid == selected
                )
            }