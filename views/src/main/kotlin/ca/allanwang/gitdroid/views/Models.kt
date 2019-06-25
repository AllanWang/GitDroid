package ca.allanwang.gitdroid.views

import android.os.Parcelable
import android.view.View
import androidx.annotation.DrawableRes
import ca.allanwang.gitdroid.data.GitObjectID
import github.fragment.*
import kotlinx.android.parcel.Parcelize
import java.net.URI
import java.util.*

@Parcelize
data class GitNameAndOwner(val name: String, val owner: String) : Parcelable {
    val nameWithOwner: String
        get() = "$owner/$name"
}

fun ShortIssueRowItem.nameAndOwner(): GitNameAndOwner = repository.fragments.repoNameAndOwner.nameAndOwner()
fun ShortPullRequestRowItem.nameAndOwner(): GitNameAndOwner = repository.fragments.repoNameAndOwner.nameAndOwner()
fun ShortRepoRowItem.nameAndOwner(): GitNameAndOwner = fragments.repoNameAndOwner.nameAndOwner()
fun RepoNameAndOwner.nameAndOwner(): GitNameAndOwner = GitNameAndOwner(name = name, owner = owner.login)

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

fun ShortIssueRowItem.issueOrPr(): GitIssueOrPr = GitIssueOrPr(
    id = id,
    databaseId = databaseId,
    url = url,
    avatarUrl = author?.fragments?.shortActor?.avatarUrl,
    login = author?.fragments?.shortActor?.login,
    createdAt = createdAt,
    number = number,
    title = title,
    nameAndOwner = nameAndOwner(),
    commentCount = comments.totalCount,
    locked = isLocked
)

fun ShortPullRequestRowItem.issueOrPr(): GitIssueOrPr = GitIssueOrPr(
    id = id,
    databaseId = databaseId,
    url = url,
    avatarUrl = author?.fragments?.shortActor?.avatarUrl,
    login = author?.fragments?.shortActor?.login,
    createdAt = createdAt,
    number = number,
    title = title,
    nameAndOwner = nameAndOwner(),
    commentCount = comments.totalCount,
    locked = isLocked
)

data class SlimEntry(@DrawableRes val icon: Int, val text: String, val onClick: ((View) -> Unit)? = null)

@Parcelize
data class PathCrumb(val segment: String, val oid: GitObjectID) : Parcelable