package ca.allanwang.gitdroid.views

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import github.GetProfileQuery
import java.util.*

private fun RecyclerView.reset() {
    (adapter as? Adapter)?.data = emptyList()
}

@BindingAdapter("pinnedItems")
fun RecyclerView.pinnedItems(
    items: GetProfileQuery.PinnedItems?
) {
    val pinned = items?.pinnedItems ?: return reset()
    val adapter = Adapter.bind(this)
    val models: List<VHBindingType> = pinned.mapNotNull { item ->
        when (item) {
            is GetProfileQuery.AsPinnableItem -> item.fragments.shortRepoRowItem?.let { RepoVhBinding(it) }
            else -> throw RuntimeException("Invalid pinned item type ${item.__typename} ${item::class.java.simpleName}")
        }
    }
    adapter.data = models
}

@BindingAdapter("slimItems")
fun RecyclerView.slimItems(
    user: GetProfileQuery.User?
) {
    user ?: return reset()

    val adapter = Adapter.bind(this)
    val dateFormat = Locale.getDefault().bestDateFormat("yyyyMMMdd")
    val models = listOf(
        SlimEntry(
            R.drawable.ic_event,
            context.getString(R.string.member_since_s, dateFormat.format(user.createdAt))
        ),
        SlimEntry(
            R.drawable.ic_followers,
            context.quantityN(R.plurals.followers_n, user.followers.totalCount)
        ),
        SlimEntry(
            R.drawable.ic_following,
            context.quantityN(R.plurals.following_n, user.following.totalCount)
        ),
        SlimEntry(
            R.drawable.ic_repo,
            context.quantityN(R.plurals.repos_n, user.repositories.totalCount)
        ),
        SlimEntry(
            R.drawable.ic_code,
            context.quantityN(R.plurals.gists_n, user.gists.totalCount)
        )
    )
    adapter.data = models.map { SlimEntryVhBinding(it) }
}