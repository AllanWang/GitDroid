package ca.allanwang.gitdroid.views.item

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import ca.allanwang.gitdroid.views.GitIssueOrPr
import ca.allanwang.gitdroid.views.PathCrumb
import ca.allanwang.gitdroid.views.R
import ca.allanwang.gitdroid.views.SlimEntry
import ca.allanwang.gitdroid.views.databinding.*
import com.mikepenz.fastadapter.FastAdapter
import github.GetProfileQuery
import github.fragment.ShortIssueRowItem
import github.fragment.ShortPullRequestRowItem
import github.fragment.ShortRepoRowItem
import github.fragment.TreeEntryItem

open class BlankViewHolderBinding(override val layoutRes: Int) : BindingItem<ViewDataBinding>(Unit)

abstract class IssuePrVhBinding(override val data: GitIssueOrPr, override val type: Int) :
    BindingItem<ViewIssueOrPrItemBinding>(data), BindingLayout<ViewIssueOrPrItemBinding> by Companion {

    override fun ViewIssueOrPrItemBinding.unbindView(holder: ViewHolder) {
        unbindGlide(iprAvatar)
        unbind(iprLogin, iprDate, iprTitle, iprDetails, iprComments)
    }

    companion object : BindingLayout<ViewIssueOrPrItemBinding> {
        override val layoutRes: Int
            get() = R.layout.view_issue_or_pr_item
    }
}

class IssueVhBinding(data: ShortIssueRowItem) :
    IssuePrVhBinding(GitIssueOrPr.fromIssue(data), R.id.git_vh_issue)

class PullRequestVhBinding(data: ShortPullRequestRowItem) :
    IssuePrVhBinding(GitIssueOrPr.fromPullRequest(data), R.id.git_vh_pr)


class RepoVhBinding(override val data: ShortRepoRowItem) :
    BindingItem<ViewRepoBinding>(data), BindingLayout<ViewRepoBinding> by Companion {

    override fun ViewRepoBinding.unbindView(holder: ViewHolder) {
        unbind(repoName, repoDesc, repoStars, repoForks, repoIssues, repoPrs, repoLanguage, repoDate)
    }

    companion object : BindingLayout<ViewRepoBinding> {
        override val layoutRes: Int
            get() = R.layout.view_repo
    }
}

class SlimEntryVhBinding(override val data: SlimEntry) :
    BindingItem<ViewSlimEntryBinding>(data), BindingLayout<ViewSlimEntryBinding> by Companion {

    override fun ViewSlimEntryBinding.unbindView(holder: ViewHolder) {
        unbind(slimIcon)
        unbind(slimText)
    }

    companion object : BindingLayout<ViewSlimEntryBinding> {
        override val layoutRes: Int
            get() = R.layout.view_slim_entry
    }
}

class UserHeaderVhBinding(override val data: GetProfileQuery.User) :
    BindingItem<ViewUserHeaderBinding>(data), BindingLayout<ViewUserHeaderBinding> by Companion {

    override fun ViewUserHeaderBinding.unbindView(holder: ViewHolder) {
        unbindGlide(userHeaderAvatar)
        unbind(userHeaderFollowToggle)
        unbind(userHeaderName, userHeaderEmail, userHeaderWeb, userHeaderLocation, userHeaderDesc)
    }

    companion object : BindingLayout<ViewUserHeaderBinding> {
        override val layoutRes: Int
            get() = R.layout.view_user_header
    }
}

class UserContributionVhBinding(override val data: GetProfileQuery.User) :
    BindingItem<ViewUserContributionsBinding>(data), BindingLayout<ViewUserContributionsBinding> by Companion {

    override fun ViewUserContributionsBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        model = data.contributionsCollection.fragments.shortContributions
    }

    override fun ViewUserContributionsBinding.unbindView(holder: ViewHolder) {
        userContributions.contributions = null
    }

    companion object : BindingLayout<ViewUserContributionsBinding> {
        override val layoutRes: Int
            get() = R.layout.view_user_contributions
    }

}

class PathCrumbVhBinding(override val data: PathCrumb) :
    BindingItem<ViewPathCrumbBinding>(data), BindingLayout<ViewPathCrumbBinding> by Companion {

    override fun ViewPathCrumbBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        model = data
        val adapter = root.getTag(R.id.fastadapter_item_adapter) as? FastAdapter<PathCrumbVhBinding> ?: return
        val isLast = adapter.getPosition(this@PathCrumbVhBinding) == adapter.itemCount - 1
        pathText.alpha = if (isLast) 1f else 0.7f
    }

    override fun ViewPathCrumbBinding.unbindView(holder: ViewHolder) {
        unbind(pathText)
    }

    companion object : BindingLayout<ViewPathCrumbBinding> {
        override val layoutRes: Int
            get() = R.layout.view_path_crumb
    }
}

object PathCrumbHomeVhBinding : BlankViewHolderBinding(R.layout.view_path_crumb_home)

class TreeEntryVhBinding(override val data: TreeEntryItem) :
    BindingItem<ViewTreeEntryBinding>(data), BindingLayout<ViewTreeEntryBinding> by Companion {

    override fun ViewTreeEntryBinding.unbindView(holder: ViewHolder) {
        unbind(treeEntryIcon)
        unbind(treeEntryText, treeEntrySize)
    }

    companion object : BindingLayout<ViewTreeEntryBinding> {

        override val layoutRes: Int
            get() = R.layout.view_tree_entry

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
