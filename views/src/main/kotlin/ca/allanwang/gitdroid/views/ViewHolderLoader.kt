package ca.allanwang.gitdroid.views

import android.content.Context
import github.GetProfileQuery
import github.fragment.ShortIssueRowItem
import github.fragment.ShortPullRequestRowItem
import github.fragment.ShortRepoRowItem
import java.util.*

fun ShortIssueRowItem.vh(): VHBindingType = IssueVhBinding(this)
fun ShortPullRequestRowItem.vh(): VHBindingType = PullRequestVhBinding(this)
fun ShortRepoRowItem.vh(): VHBindingType = RepoVhBinding(this)
fun SlimEntry.vh(): VHBindingType = SlimEntryVhBinding(this)

fun GetProfileQuery.User.vhFull(context: Context): List<VHBindingType> {
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
    return mutableListOf<VHBindingType>().apply {
        add(vhHeader())
        addAll(slimModels.map { it.vh() })
        add(vhContribution())
        addAll(pinnedItems.vhList())
    }
}

fun GetProfileQuery.User.vhHeader(): VHBindingType = UserHeaderVhBinding(this)


fun GetProfileQuery.User.vhContribution(): VHBindingType = UserContributionVhBinding(this)
