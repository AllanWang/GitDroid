package ca.allanwang.gitdroid.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.ktx.utils.L
import ca.allanwang.gitdroid.views.databinding.*
import github.GetProfileQuery
import github.fragment.ShortIssueRowItem
import github.fragment.ShortPullRequestRowItem
import github.fragment.ShortRepoRowItem
import github.fragment.TreeEntryItem

typealias VHBindingType = ViewHolderBinding<*>

abstract class ViewHolderBinding<T : ViewDataBinding>(
    open val data: Any?,
    open val layoutRes: Int,
    open val typeId: Int = layoutRes
) {

    abstract val dataId: Any?

    open fun T.create() {}

    open fun T.bind(info: BindInfo, payloads: MutableList<Any>) {
        if (!setVariable(BR.model, data)) {
            L.fail { "Could not bind model to ${this::class.java.simpleName}" }
        }
    }

    open fun T.onRecycled() {
        if (!setVariable(BR.model, null)) {
            L.fail { "Could not unbind model to ${this::class.java.simpleName}" }
        }
    }

    fun onCreate(parent: ViewGroup): View {
        val binding: T = DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutRes, parent, false)
        binding.create()
        return binding.root
    }

    fun onBind(holder: RecyclerView.ViewHolder, info: BindInfo, payloads: MutableList<Any>) {
        val binding: T = DataBindingUtil.getBinding(holder.itemView) ?: return
        binding.bind(info, payloads)
    }

    fun onRecycled(holder: RecyclerView.ViewHolder) {
        val binding: T = DataBindingUtil.getBinding(holder.itemView) ?: return
        binding.onRecycled()
        binding.unbind()
    }

    open fun onClick(view: View, info: ClickInfo) {}

    open fun isItemSame(vh: VHBindingType): Boolean = typeId == vh.typeId && dataId != null && dataId == vh.dataId
    open fun isContentSame(vh: VHBindingType): Boolean = typeId == vh.typeId && data == vh.data
    open fun changePayload(vh: VHBindingType): Any? = vh.data

}

data class BindInfo(val position: Int, val totalCount: Int)

data class ClickInfo(val position: Int, val totalCount: Int)

abstract class IssuePrVhBinding(override val data: GitIssueOrPr, override val typeId: Int) :
    ViewHolderBinding<ViewIssueOrPrItemBinding>(data, R.layout.view_issue_or_pr_item) {

    override val dataId: Int?
        get() = data.databaseId
}

class IssueVhBinding(data: ShortIssueRowItem) :
    IssuePrVhBinding(GitIssueOrPr.fromIssue(data), R.id.git_vh_issue)

class PullRequestVhBinding(data: ShortPullRequestRowItem) :
    IssuePrVhBinding(GitIssueOrPr.fromPullRequest(data), R.id.git_vh_pr)


class RepoVhBinding(override val data: ShortRepoRowItem) :
    ViewHolderBinding<ViewRepoBinding>(data, R.layout.view_repo) {
    override val dataId: Int?
        get() = data.databaseId
}

class SlimEntryVhBinding(override val data: SlimEntry) :
    ViewHolderBinding<ViewSlimEntryBinding>(data, R.layout.view_slim_entry) {
    override val dataId: Int?
        get() = data.icon

    override fun onClick(view: View, info: ClickInfo) {
        super.onClick(view, info)
        data.onClick?.invoke(view)
    }
}

class UserHeaderVhBinding(override val data: GetProfileQuery.User) :
    ViewHolderBinding<ViewUserHeaderBinding>(data, R.layout.view_user_header) {
    override val dataId: Int?
        get() = data.databaseId
}

class UserContributionVhBinding(override val data: GetProfileQuery.User) :
    ViewHolderBinding<ViewUserContributionsBinding>(data, R.layout.view_user_contributions) {
    override val dataId: Int?
        get() = data.databaseId

    override fun ViewUserContributionsBinding.bind(info: BindInfo, payloads: MutableList<Any>) {
        model = data.contributionsCollection.fragments.shortContributions
    }
}

class PathCrumbVhBinding(override val data: PathCrumb) :
    ViewHolderBinding<ViewPathCrumbBinding>(data, R.layout.view_path_crumb) {
    override val dataId: String
        get() = data.fullPath

    override fun ViewPathCrumbBinding.bind(info: BindInfo, payloads: MutableList<Any>) {
        model = data
        val isLast = info.position == info.totalCount - 1
        pathText.alpha = if (isLast) 1f else 0.7f
    }
}

class TreeEntryVhBinding(override val data: TreeEntryItem) :
    ViewHolderBinding<ViewTreeEntryBinding>(data, R.layout.view_tree_entry) {
    override val dataId: GitObjectID
        get() = data.oid // todo verify

   companion object {

       @BindingAdapter("treeEntrySrc")
       @JvmStatic
       fun ImageView.treeEntrySrc(obj: TreeEntryItem?) {
           val src = when (obj?.obj) {
               null -> null
               is TreeEntryItem.AsBlob -> R.drawable.ic_file
               else -> R.drawable.ic_folder
           }
           if (src == null) {
               setImageDrawable(null)
           } else {
               setImageResource(src)
           }
       }

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

