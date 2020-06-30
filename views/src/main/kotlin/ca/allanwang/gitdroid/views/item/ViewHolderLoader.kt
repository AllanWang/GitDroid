package ca.allanwang.gitdroid.views.item

import android.content.Context
import ca.allanwang.gitdroid.views.R
import ca.allanwang.gitdroid.views.utils.*
import com.mikepenz.fastadapter.GenericItem
import github.GetProfileQuery
import github.fragment.*
import java.util.*

fun ShortIssueRowItem.vh(): GenericItem = IssueVhBinding(this)
fun ShortPullRequestRowItem.vh(): GenericItem =
    PullRequestVhBinding(this)

fun ShortRepoRowItem.vh(): GenericItem = RepoVhBinding(this)
fun SlimEntry.vh(): GenericItem =
    SlimEntryVhBinding(this)

fun GetProfileQuery.User.vhFull(context: Context): List<GenericItem> {
    val dateFormat = Locale.getDefault().bestDateFormat("yyyyMMMdd")
    val slimModels = listOf(
        SlimEntry(
            R.drawable.ic_event,
            context.getString(R.string.member_since_s, dateFormat.format(createdAt))
        ),
        SlimEntry(
            R.drawable.ic_followers,
            context.quantityN(R.plurals.followers_n, followers.totalCount)
        ),
        SlimEntry(
            R.drawable.ic_following,
            context.quantityN(R.plurals.following_n, following.totalCount)
        ),
        SlimEntry(
            R.drawable.ic_repo,
            context.quantityN(R.plurals.repos_n, repositories.totalCount)
        ),
        SlimEntry(
            R.drawable.ic_code,
            context.quantityN(R.plurals.gists_n, gists.totalCount)
        )
    )
    return mutableListOf<GenericItem>().apply {
        add(vhHeader())
        addAll(slimModels.map { it.vh() })
        add(vhContribution())
        addAll(pinnedItems.vhList())
    }
}

fun GetProfileQuery.PinnedItems.vhList(): List<GenericItem> = pinnedItems?.mapNotNull { item ->
    when (item) {
        is GetProfileQuery.AsPinnableItem -> item.fragments.shortRepoRowItem?.vh()
        else -> throw RuntimeException("Invalid pinned item type ${item.__typename} ${item::class.java.simpleName}")
    }
} ?: emptyList()

fun GetProfileQuery.User.vhHeader(): GenericItem =
    UserHeaderVhBinding(this)


fun GetProfileQuery.User.vhContribution(): GenericItem =
    UserContributionVhBinding(this)

fun PathCrumb.vh(): GenericItem =
    PathCrumbVhBinding(this)

fun TreeEntryItem.vh(): GenericItem = TreeEntryVhBinding(this)

fun ShortIssueComment.vh(): GenericItem = IssueCommentVhBinding(this)

fun RefEntry.vh(): GenericItem = RefEntryVhBinding(this)

fun ShortRepoRowItem.vhHeader(): GenericItem = RepoOverviewHeaderVhBinding(this)

fun FullRepo.vhFull(context: Context): List<GenericItem> {
    val slimModels = with(fragments.shortRepoRowItem) {
        listOf(
            SlimEntry(
                R.drawable.ic_issue,
                context.quantityN(R.plurals.issues_n, issues.totalCount)
            ),
            SlimEntry(
                R.drawable.ic_pull_request,
                context.quantityN(R.plurals.pull_requests_n, pullRequests.totalCount)
            ),
            SlimEntry(
                R.drawable.ic_fork,
                context.quantityN(R.plurals.forks_n, forks.totalCount)
            ),
            SlimEntry(
                R.drawable.ic_release,
                context.quantityN(R.plurals.releases_n, releases.totalCount)
            ),
            SlimEntry(
                R.drawable.ic_star,
                context.quantityN(R.plurals.stars_n, stargazers.totalCount)
            ),
            SlimEntry(
                R.drawable.ic_eye,
                context.quantityN(R.plurals.releases_n, watchers.totalCount)
            )
        )
    }
    return mutableListOf<GenericItem>().apply {
        add(fragments.shortRepoRowItem.vhHeader())
        addAll(slimModels.map { it.vh() })
    }
}