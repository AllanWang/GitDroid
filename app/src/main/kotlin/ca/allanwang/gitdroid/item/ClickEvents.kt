package ca.allanwang.gitdroid.item

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.activity.IssueCommentActivity
import ca.allanwang.gitdroid.activity.RepoActivity
import ca.allanwang.gitdroid.activity.UserActivity
import ca.allanwang.gitdroid.views.databinding.ViewIssueCommentBinding
import ca.allanwang.gitdroid.views.databinding.ViewIssueOrPrItemBinding
import ca.allanwang.gitdroid.views.databinding.ViewRepoBinding
import ca.allanwang.gitdroid.views.databinding.ViewSlimEntryBinding
import ca.allanwang.gitdroid.views.item.*
import ca.allanwang.gitdroid.data.gitNameAndOwner
import ca.allanwang.gitdroid.data.gitRef
import com.mikepenz.fastadapter.FastAdapter

fun SlimEntryVhBinding.Companion.clickHook() =
    object : BindingClickEventHook<ViewSlimEntryBinding, SlimEntryVhBinding>(SlimEntryVhBinding) {

        override fun ViewSlimEntryBinding.onBind(viewHolder: RecyclerView.ViewHolder): View? = root

        override fun ViewSlimEntryBinding.onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<SlimEntryVhBinding>,
            item: SlimEntryVhBinding
        ) {
            item.data.onClick?.invoke(v)
        }

    }

fun IssuePrVhBinding.Companion.clickHook() =
    object : BindingClickEventHook<ViewIssueOrPrItemBinding, IssuePrVhBinding>(IssuePrVhBinding) {
        override fun ViewIssueOrPrItemBinding.onBind(viewHolder: RecyclerView.ViewHolder): View? = root
        override fun ViewIssueOrPrItemBinding.onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<IssuePrVhBinding>,
            item: IssuePrVhBinding
        ) {
            IssueCommentActivity.launch(v.context, item.data.nameAndOwner, item.data.title, item.data.number, root)
        }
    }

fun RepoVhBinding.Companion.clickHook() =
    object : BindingClickEventHook<ViewRepoBinding, RepoVhBinding>(RepoVhBinding) {

        override fun ViewRepoBinding.onBind(viewHolder: RecyclerView.ViewHolder): View? = root

        override fun ViewRepoBinding.onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<RepoVhBinding>,
            item: RepoVhBinding
        ) {
            RepoActivity.launch(v.context, item.data.gitNameAndOwner(), item.data.defaultBranchRef?.fragments?.shortRef?.gitRef())
        }
    }

fun IssueCommentVhBinding.Companion.clickHook() =
    object : BindingClickEventHook<ViewIssueCommentBinding, IssueCommentVhBinding>(IssueCommentVhBinding) {
        override fun ViewIssueCommentBinding.onBindMany(viewHolder: RecyclerView.ViewHolder): List<View>? {
            return listOf(avatar, reaction, more)
        }

        override fun ViewIssueCommentBinding.onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<IssueCommentVhBinding>,
            item: IssueCommentVhBinding
        ) {
            when (v) {
                avatar -> {
                    val login = item.data.fragments.shortComment?.author?.fragments?.shortActor?.login ?: return
                    UserActivity.launch(v.context, login)
                }
            }
        }

    }