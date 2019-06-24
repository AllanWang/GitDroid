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
    BindingItem<ViewIssueOrPrItemBinding>(data) {

    override val layoutRes: Int
        get() = R.layout.view_issue_or_pr_item

    override fun ViewIssueOrPrItemBinding.unbindView(holder: ViewHolder) {
        unbindGlide(iprAvatar)
        unbind(iprLogin, iprDate, iprTitle, iprDetails, iprComments)
    }
}

class IssueVhBinding(data: ShortIssueRowItem) :
    IssuePrVhBinding(GitIssueOrPr.fromIssue(data), R.id.git_vh_issue)

class PullRequestVhBinding(data: ShortPullRequestRowItem) :
    IssuePrVhBinding(GitIssueOrPr.fromPullRequest(data), R.id.git_vh_pr)


class RepoVhBinding(override val data: ShortRepoRowItem) :
    BindingItem<ViewRepoBinding>(data) {

    override val layoutRes: Int
        get() = R.layout.view_repo

    override fun ViewRepoBinding.unbindView(holder: ViewHolder) {
        unbind(repoName, repoDesc, repoStars, repoForks, repoIssues, repoPrs, repoLanguage, repoDate)
    }

    companion object
}

class SlimEntryVhBinding(override val data: SlimEntry) :
    BindingItem<ViewSlimEntryBinding>(data) {

    override val layoutRes: Int
        get() = R.layout.view_slim_entry

    override fun ViewSlimEntryBinding.unbindView(holder: ViewHolder) {
        unbind(slimIcon)
        unbind(slimText)
    }

    companion object
}

class UserHeaderVhBinding(override val data: GetProfileQuery.User) :
    BindingItem<ViewUserHeaderBinding>(data) {

    override val layoutRes: Int
        get() = R.layout.view_user_header

    override fun ViewUserHeaderBinding.unbindView(holder: ViewHolder) {
        unbindGlide(userHeaderAvatar)
        unbind(userHeaderFollowToggle)
        unbind(userHeaderName, userHeaderEmail, userHeaderWeb, userHeaderLocation, userHeaderDesc)
    }

}

class UserContributionVhBinding(override val data: GetProfileQuery.User) :
    BindingItem<ViewUserContributionsBinding>(data) {

    override val layoutRes: Int
        get() = R.layout.view_user_contributions

    override fun ViewUserContributionsBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        model = data.contributionsCollection.fragments.shortContributions
    }

    override fun ViewUserContributionsBinding.unbindView(holder: ViewHolder) {
        userContributions.contributions = null
    }

}

class PathCrumbVhBinding(override val data: PathCrumb) :
    BindingItem<ViewPathCrumbBinding>(data) {

    override val layoutRes: Int
        get() = R.layout.view_path_crumb

    override fun ViewPathCrumbBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        model = data
        val adapter = root.getTag(R.id.fastadapter_item_adapter) as? FastAdapter<PathCrumbVhBinding> ?: return
        val isLast = adapter.getPosition(this@PathCrumbVhBinding) == adapter.itemCount - 1
        pathText.alpha = if (isLast) 1f else 0.7f
    }

    override fun ViewPathCrumbBinding.unbindView(holder: ViewHolder) {
        unbind(pathText)
    }
}

object PathCrumbHomeVhBinding : BlankViewHolderBinding(R.layout.view_path_crumb_home)

class TreeEntryVhBinding(override val data: TreeEntryItem) :
    BindingItem<ViewTreeEntryBinding>(data) {

    override val layoutRes: Int
        get() = R.layout.view_tree_entry

    override fun ViewTreeEntryBinding.unbindView(holder: ViewHolder) {
        unbind(treeEntryIcon)
        unbind(treeEntryText, treeEntrySize)
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
