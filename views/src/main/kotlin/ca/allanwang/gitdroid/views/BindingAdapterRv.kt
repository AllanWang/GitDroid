package ca.allanwang.gitdroid.views

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import github.GetProfileQuery
import github.fragment.ShortRepoRowItem
import java.util.*


@BindingAdapter("pinnedItems")
fun RecyclerView.pinnedItems(
    items: GetProfileQuery.PinnedItems
) {
    val adapter = Adapter.bind(this)
    val models: List<VHBindingType> = items.pinnedItems?.map {
        when (it) {
            is ShortRepoRowItem -> RepoVhBinding(it)
            else -> throw RuntimeException("Invalid pinned item type ${it.__typename}")
        }
    } ?: emptyList()
    adapter.data = models
}

@BindingAdapter("slimItems")
fun RecyclerView.slimItems(
    user: GetProfileQuery.User
) {
    val adapter = Adapter.bind(this)
    val dateFormat = Locale.getDefault().bestDateFormat("yyyyMMMdd")
    val models = listOf(
        SlimEntry(
            R.drawable.ic_event,
            context.getString(R.string.member_since_s, dateFormat.format(user.createdAt))
        ),
        SlimEntry(
            R.drawable.ic_people,
            context.quantityN(R.plurals.followers_n, user.followers.totalCount)
        ),
        SlimEntry(
            R.drawable.ic_group,
            context.quantityN(R.plurals.following_n, user.following.totalCount)
        ),
        SlimEntry(
            R.drawable.ic_repo,
            context.quantityN(R.plurals.repos_n, user.repositories.totalCount)
        ),
        SlimEntry(
            R.drawable.ic_code,
            context.quantityN(R.plurals.repos_n, user.repositories.totalCount)
        )
    )
    adapter.data = models.map { SlimEntryVhBinding(it) }
}