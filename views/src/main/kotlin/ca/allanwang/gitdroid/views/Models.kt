package ca.allanwang.gitdroid.views

import android.view.View
import androidx.annotation.DrawableRes
import github.fragment.ShortIssueRowItem
import github.fragment.ShortPullRequestRowItem
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
    val nameWithOwner: String,
    val commentCount: Int,
    val locked: Boolean
) {
    companion object {
        fun fromIssue(issue: ShortIssueRowItem) = GitIssueOrPr(
            id = issue.id,
            databaseId = issue.databaseId,
            url = issue.url,
            avatarUrl = issue.author?.fragments?.shortActor?.avatarUrl,
            login = issue.author?.fragments?.shortActor?.login,
            createdAt = issue.createdAt,
            number = issue.number,
            title = issue.title,
            nameWithOwner = issue.repository.nameWithOwner,
            commentCount = issue.comments.totalCount,
            locked = issue.isLocked
        )

        fun fromPullRequest(pr: ShortPullRequestRowItem) = GitIssueOrPr(
            id = pr.id,
            databaseId = pr.databaseId,
            url = pr.url,
            avatarUrl = pr.author?.fragments?.shortActor?.avatarUrl,
            login = pr.author?.fragments?.shortActor?.login,
            createdAt = pr.createdAt,
            number = pr.number,
            title = pr.title,
            nameWithOwner = pr.repository.nameWithOwner,
            commentCount = pr.comments.totalCount,
            locked = pr.isLocked
        )
    }
}

data class SlimEntry(@DrawableRes val icon: Int, val text: String, val onClick: ((View) -> Unit)? = null)

data class PathCrumb(val segment: String, val fullPath: String)
