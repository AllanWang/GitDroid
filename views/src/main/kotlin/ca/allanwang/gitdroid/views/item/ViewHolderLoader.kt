package ca.allanwang.gitdroid.views.item

import android.content.Context
import ca.allanwang.gitdroid.views.*
import ca.allanwang.gitdroid.views.utils.*
import github.GetProfileQuery
import github.fragment.*
import java.util.*

fun ShortIssueRowItem.vh(): GenericBindingItem = IssueVhBinding(this)
fun ShortPullRequestRowItem.vh(): GenericBindingItem =
    PullRequestVhBinding(this)

fun ShortRepoRowItem.vh(): GenericBindingItem = RepoVhBinding(this)
fun SlimEntry.vh(): GenericBindingItem =
    SlimEntryVhBinding(this)

fun GetProfileQuery.User.vhFull(context: Context): List<GenericBindingItem> {
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
    return mutableListOf<GenericBindingItem>().apply {
        add(vhHeader())
        addAll(slimModels.map { it.vh() })
        add(vhContribution())
        addAll(pinnedItems.vhList())
    }
}

fun GetProfileQuery.PinnedItems.vhList(): List<GenericBindingItem> = pinnedItems?.mapNotNull { item ->
    when (item) {
        is GetProfileQuery.AsPinnableItem -> item.fragments.shortRepoRowItem?.vh()
        else -> throw RuntimeException("Invalid pinned item type ${item.__typename} ${item::class.java.simpleName}")
    }
} ?: emptyList()

fun GetProfileQuery.User.vhHeader(): GenericBindingItem =
    UserHeaderVhBinding(this)


fun GetProfileQuery.User.vhContribution(): GenericBindingItem =
    UserContributionVhBinding(this)

fun PathCrumb.vh(): GenericBindingItem =
    PathCrumbVhBinding(this)

fun TreeEntryItem.vh(): GenericBindingItem = TreeEntryVhBinding(this)

fun ShortIssueComment.vh(): GenericBindingItem = IssueCommentVhBinding(this)

fun RefEntry.vh(): GenericBindingItem = RefEntryVhBinding(this)