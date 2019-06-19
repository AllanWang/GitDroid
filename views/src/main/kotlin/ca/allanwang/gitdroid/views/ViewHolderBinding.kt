package ca.allanwang.gitdroid.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.databinding.*
import github.GetProfileQuery
import github.fragment.ShortIssueRowItem
import github.fragment.ShortPullRequestRowItem
import github.fragment.ShortRepoRowItem

typealias VHBindingType = ViewHolderBinding<*>

abstract class ViewHolderBinding<T : ViewDataBinding>(
    open val data: Any?,
    open val layoutRes: Int,
    open val typeId: Int = layoutRes
) {

    abstract val dataId: Int?

    open fun T.create() {}

    abstract fun T.bind(position: Int, payloads: MutableList<Any>)

    open fun T.onRecycled() {}

    fun onCreate(parent: ViewGroup): View {
        val binding: T = DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutRes, parent, false)
        binding.create()
        return binding.root
    }

    fun onBind(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val binding: T = DataBindingUtil.getBinding(holder.itemView) ?: return
        binding.bind(position, payloads)
    }

    fun onRecycled(holder: RecyclerView.ViewHolder) {
        val binding: T = DataBindingUtil.getBinding(holder.itemView) ?: return
        binding.onRecycled()
        binding.unbind()
    }

    open fun onClick(view: View, position: Int) {}

    open fun isItemSame(vh: VHBindingType): Boolean = typeId == vh.typeId && dataId != null && dataId == vh.dataId
    open fun isContentSame(vh: VHBindingType): Boolean = typeId == vh.typeId && data == vh.data
    open fun changePayload(vh: VHBindingType): Any? = vh.data

}

abstract class IssuePrVhBinding(override val data: GitIssueOrPr, override val typeId: Int) :
    ViewHolderBinding<ViewIssueOrPrItemBinding>(data, R.layout.view_issue_or_pr_item) {

    override val dataId: Int?
        get() = data.databaseId

    override fun ViewIssueOrPrItemBinding.bind(position: Int, payloads: MutableList<Any>) {
        model = data
    }
}

class IssueVhBinding(data: ShortIssueRowItem) :
    IssuePrVhBinding(GitIssueOrPr.fromIssue(data), R.id.git_vh_issue)

class PullRequestVhBinding(data: ShortPullRequestRowItem) :
    IssuePrVhBinding(GitIssueOrPr.fromPullRequest(data), R.id.git_vh_pr)

class RepoVhBinding(override val data: ShortRepoRowItem) :
    ViewHolderBinding<ViewRepoBinding>(data, R.layout.view_repo) {
    override val dataId: Int?
        get() = data.databaseId

    override fun ViewRepoBinding.bind(position: Int, payloads: MutableList<Any>) {
        repo = data
    }
}

class SlimEntryVhBinding(override val data: SlimEntry) :
    ViewHolderBinding<ViewSlimEntryBinding>(data, R.layout.view_slim_entry) {
    override val dataId: Int?
        get() = data.icon

    override fun ViewSlimEntryBinding.bind(position: Int, payloads: MutableList<Any>) {
        model = data
    }

    override fun onClick(view: View, position: Int) {
        super.onClick(view, position)
        data.onClick?.invoke(view)
    }
}

class UserHeaderVhBinding(override val data: GetProfileQuery.User) :
    ViewHolderBinding<ViewUserHeaderBinding>(data, R.layout.view_user_header) {
    override val dataId: Int?
        get() = data.databaseId

    override fun ViewUserHeaderBinding.bind(position: Int, payloads: MutableList<Any>) {
        model = data
    }
}

class UserContributionVhBinding(override val data: GetProfileQuery.User) :
    ViewHolderBinding<ViewUserContributionsBinding>(data, R.layout.view_user_contributions) {
    override val dataId: Int?
        get() = data.databaseId

    override fun ViewUserContributionsBinding.bind(position: Int, payloads: MutableList<Any>) {
        model = data.contributionsCollection.fragments.shortContributions
    }
}
