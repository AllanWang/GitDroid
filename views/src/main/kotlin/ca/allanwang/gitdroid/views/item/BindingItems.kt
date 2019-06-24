package ca.allanwang.gitdroid.views.item

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.*
import ca.allanwang.gitdroid.views.databinding.*
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import github.GetProfileQuery
import github.fragment.ShortIssueRowItem
import github.fragment.ShortPullRequestRowItem
import github.fragment.ShortRepoRowItem
import github.fragment.TreeEntryItem

open class BlankViewHolderBinding(
    override val layoutRes: Int
) : BindingItem<BlankViewHolderBinding.ViewHolder>(Unit) {

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(itemView: View) : BindingViewHolder<IssuePrVhBinding, ViewDataBinding>(itemView)
}

abstract class IssuePrVhBinding(override val data: GitIssueOrPr, override val type: Int) :
    BindingItem<IssuePrVhBinding.ViewHolder>(data) {

    override val layoutRes: Int
        get() = R.layout.view_issue_or_pr_item

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(itemView: View) : BindingViewHolder<IssuePrVhBinding, ViewIssueOrPrItemBinding>(itemView) {
        override fun ViewIssueOrPrItemBinding.unbindView(item: IssuePrVhBinding) {
            unbindGlide(iprAvatar)
            unbind(iprLogin, iprDate, iprTitle, iprDetails, iprComments)
        }

    }
}

class IssueVhBinding(data: ShortIssueRowItem) :
    IssuePrVhBinding(GitIssueOrPr.fromIssue(data), R.id.git_vh_issue)

class PullRequestVhBinding(data: ShortPullRequestRowItem) :
    IssuePrVhBinding(GitIssueOrPr.fromPullRequest(data), R.id.git_vh_pr)


class RepoVhBinding(override val data: ShortRepoRowItem) :
    BindingItem<RepoVhBinding.ViewHolder>(data) {

    override val layoutRes: Int
        get() = R.layout.view_repo

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(itemView: View) : BindingViewHolder<IssuePrVhBinding, ViewRepoBinding>(itemView) {
        override fun ViewRepoBinding.unbindView(item: IssuePrVhBinding) {
            unbind(repoName, repoDesc, repoStars, repoForks, repoIssues, repoPrs, repoLanguage, repoDate)
        }
    }
}

class SlimEntryVhBinding(override val data: SlimEntry) :
    BindingItem<SlimEntryVhBinding.ViewHolder>(data) {

    override val layoutRes: Int
        get() = R.layout.view_slim_entry

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(itemView: View) : BindingViewHolder<SlimEntryVhBinding, ViewSlimEntryBinding>(itemView) {

        override fun ViewSlimEntryBinding.unbindView(item: SlimEntryVhBinding) {
            unbind(slimIcon)
            unbind(slimText)
        }
    }

    companion object {
        fun clickHook(): ClickEventHook<SlimEntryVhBinding> = object : ClickEventHook<SlimEntryVhBinding>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? = (viewHolder as? ViewHolder)?.binding?.root

            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<SlimEntryVhBinding>,
                item: SlimEntryVhBinding
            ) {
                item.data.onClick?.invoke(v)
            }

        }
    }
}

class UserHeaderVhBinding(override val data: GetProfileQuery.User) :
    BindingItem<UserHeaderVhBinding.ViewHolder>(data) {

    override val layoutRes: Int
        get() = R.layout.view_user_header

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(itemView: View) : BindingViewHolder<UserHeaderVhBinding, ViewUserHeaderBinding>(itemView) {
        override fun ViewUserHeaderBinding.unbindView(item: UserHeaderVhBinding) {
            unbindGlide(userHeaderAvatar)
            unbind(userHeaderFollowToggle)
            unbind(userHeaderName, userHeaderEmail, userHeaderWeb, userHeaderLocation, userHeaderDesc)
        }
    }
}

class UserContributionVhBinding(override val data: GetProfileQuery.User) :
    BindingItem<UserContributionVhBinding.ViewHolder>(data) {

    override val layoutRes: Int
        get() = R.layout.view_user_contributions


    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(itemView: View) :
        BindingViewHolder<UserContributionVhBinding, ViewUserContributionsBinding>(itemView) {
        override fun ViewUserContributionsBinding.bindView(
            item: UserContributionVhBinding,
            payloads: MutableList<Any>
        ) {
            model = item.data.contributionsCollection.fragments.shortContributions
        }

        override fun ViewUserContributionsBinding.unbindView(item: UserContributionVhBinding) {
            userContributions.contributions = null
        }
    }
}

class PathCrumbVhBinding(override val data: PathCrumb) :
    BindingItem<PathCrumbVhBinding.ViewHolder>(data) {

    override val layoutRes: Int
        get() = R.layout.view_path_crumb

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(itemView: View) : BindingViewHolder<PathCrumbVhBinding, ViewPathCrumbBinding>(itemView) {
        override fun ViewPathCrumbBinding.bindView(item: PathCrumbVhBinding, payloads: MutableList<Any>) {
            model = item.data
            val adapter = itemView.getTag(R.id.fastadapter_item_adapter) as? FastAdapter<PathCrumbVhBinding> ?: return
            val isLast = adapter.getPosition(item) == adapter.itemCount - 1
            pathText.alpha = if (isLast) 1f else 0.7f
        }

        override fun ViewPathCrumbBinding.unbindView(item: PathCrumbVhBinding) {
            unbind(pathText)
        }
    }
}

object PathCrumbHomeVhBinding : BlankViewHolderBinding(R.layout.view_path_crumb_home)

class TreeEntryVhBinding(override val data: TreeEntryItem) :
    BindingItem<TreeEntryVhBinding.ViewHolder>(data) {

    override val layoutRes: Int
        get() = R.layout.view_tree_entry

    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    class ViewHolder(itemView: View) : BindingViewHolder<TreeEntryVhBinding, ViewTreeEntryBinding>(itemView) {
        override fun ViewTreeEntryBinding.unbindView(item: TreeEntryVhBinding) {
            unbind(treeEntryIcon)
            unbind(treeEntryText, treeEntrySize)
        }
    }

    companion object {

        @BindingAdapter("treeEntrySizeText")
        @JvmStatic
        fun TextView.treeEntrySizeText(obj: TreeEntryItem?) {
            val blob = obj?.obj as? TreeEntryItem.AsBlob
            if (blob == null) {
                text = null
            } else {
                text = blob.byteSize.toString()
            }
        }
    }
}
