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
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.gitdroid.views.databinding.*
import com.bumptech.glide.Glide
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

    /**
     * Called when view is recycled.
     * Note that simply setting a variable to null doesn't actually do anything,
     * meaning that this needs to be done manually
     */
    open fun T.onRecycled() {
    }

    protected fun glideRecycle(vararg imageView: ImageView) {
        if (imageView.isEmpty()) {
            return
        }
        val manager = Glide.with(imageView.first().context)
        imageView.forEach { manager.clear(it) }
    }

    protected fun recycle(vararg imageView: ImageView) {
        imageView.forEach { it.setImageDrawable(null) }
    }

    protected fun recycle(vararg textView: TextView) {
        textView.forEach { it.text = null }
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

open class BlankViewHolderBinding<T : ViewDataBinding>(
    override val layoutRes: Int,
    override val typeId: Int = layoutRes
) : ViewHolderBinding<T>(Unit, layoutRes, typeId) {

    // Always equal
    override val dataId: Unit = Unit

    final override fun T.bind(info: BindInfo, payloads: MutableList<Any>) {
        // no op
    }

    final override fun T.onRecycled() {
        // no op
    }

    final override fun T.create() {
        // no op
    }
}

data class BindInfo(val position: Int, val totalCount: Int) {
    val isLast: Boolean get() = position == totalCount - 1
}

data class ClickInfo(val position: Int, val totalCount: Int) {
    val isLast: Boolean get() = position == totalCount - 1
}

abstract class IssuePrVhBinding(override val data: GitIssueOrPr, override val typeId: Int) :
    ViewHolderBinding<ViewIssueOrPrItemBinding>(data, R.layout.view_issue_or_pr_item) {

    override val dataId: Int?
        get() = data.databaseId

    override fun ViewIssueOrPrItemBinding.onRecycled() {
        glideRecycle(iprAvatar)
        recycle(iprLogin, iprDate, iprTitle, iprDetails, iprComments)
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

    override fun ViewRepoBinding.onRecycled() {
        recycle(repoName, repoDesc, repoStars, repoForks, repoIssues, repoPrs, repoLanguage, repoDate)
    }
}

class SlimEntryVhBinding(override val data: SlimEntry) :
    ViewHolderBinding<ViewSlimEntryBinding>(data, R.layout.view_slim_entry) {
    override val dataId: Int?
        get() = data.icon

    override fun onClick(view: View, info: ClickInfo) {
        super.onClick(view, info)
        data.onClick?.invoke(view)
    }

    override fun ViewSlimEntryBinding.onRecycled() {
        recycle(slimIcon)
        recycle(slimText)
    }
}

class UserHeaderVhBinding(override val data: GetProfileQuery.User) :
    ViewHolderBinding<ViewUserHeaderBinding>(data, R.layout.view_user_header) {
    override val dataId: Int?
        get() = data.databaseId

    override fun ViewUserHeaderBinding.onRecycled() {
        glideRecycle(userHeaderAvatar)
        recycle(userHeaderFollowToggle)
        recycle(userHeaderName, userHeaderEmail, userHeaderWeb, userHeaderLocation, userHeaderDesc)
    }
}

class UserContributionVhBinding(override val data: GetProfileQuery.User) :
    ViewHolderBinding<ViewUserContributionsBinding>(data, R.layout.view_user_contributions) {
    override val dataId: Int?
        get() = data.databaseId

    override fun ViewUserContributionsBinding.bind(info: BindInfo, payloads: MutableList<Any>) {
        model = data.contributionsCollection.fragments.shortContributions
    }

    override fun ViewUserContributionsBinding.onRecycled() {
        userContributions.contributions = null
    }
}

class PathCrumbVhBinding(override val data: PathCrumb) :
    ViewHolderBinding<ViewPathCrumbBinding>(data, R.layout.view_path_crumb) {
    override val dataId: GitObjectID?
        get() = data.oid

    override fun ViewPathCrumbBinding.bind(info: BindInfo, payloads: MutableList<Any>) {
        model = data
        pathText.alpha = if (info.isLast) 1f else 0.7f
    }

    override fun ViewPathCrumbBinding.onRecycled() {
        recycle(pathText)
    }
}

object PathCrumbHomeVhBinding : BlankViewHolderBinding<ViewPathCrumbHomeBinding>(R.layout.view_path_crumb_home)

class TreeEntryVhBinding(override val data: TreeEntryItem) :
    ViewHolderBinding<ViewTreeEntryBinding>(data, R.layout.view_tree_entry) {
    override val dataId: GitObjectID
        get() = data.oid

    override fun ViewTreeEntryBinding.onRecycled() {
        recycle(treeEntryIcon)
        recycle(treeEntryText, treeEntrySize)
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

