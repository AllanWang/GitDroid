package ca.allanwang.gitdroid.views.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import ca.allanwang.fastadapter.viewbinding.BindingItem
import ca.allanwang.fastadapter.viewbinding.BindingLayout
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.gitNameAndOwner
import ca.allanwang.gitdroid.views.R
import ca.allanwang.gitdroid.views.databinding.*
import ca.allanwang.gitdroid.views.utils.*
import ca.allanwang.kau.utils.drawable
import com.mikepenz.fastadapter.FastAdapter
import github.GetProfileQuery
import github.fragment.*

abstract class IssuePrVhBinding(override val data: GitIssueOrPr, override val type: Int) :
    BindingItem<ViewIssueOrPrItemBinding>(data),
    BindingLayout<ViewIssueOrPrItemBinding> by Companion {

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewIssueOrPrItemBinding = ViewIssueOrPrItemBinding.inflate(layoutInflater, parent, false)

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
    IssuePrVhBinding(data.issueOrPr(), R.id.git_vh_issue) {
    override fun ViewIssueOrPrItemBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        iprAvatar.glideRound(data.avatarUrl)
        iprLogin.text = data.login
        iprDate.relativeDateText(data.createdAt)
        iprTitle.text = data.title
        iprDetails.text = "${data.nameWithOwner}#${data.number}"
        iprLocked.goneFlag(!data.locked)
        iprComments.apply {
            compactNumberText(data.commentCount)
            goneFlag(data.commentCount)
        }
    }
}

class PullRequestVhBinding(data: ShortPullRequestRowItem) :
    IssuePrVhBinding(data.issueOrPr(), R.id.git_vh_pr) {
    override fun ViewIssueOrPrItemBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        iprAvatar.glideRound(data.avatarUrl)
        iprLogin.text = data.login
        iprDate.relativeDateText(data.createdAt)
        iprTitle.text = data.title
        iprDetails.text = "${data.nameWithOwner}#${data.number}"
        iprLocked.goneFlag(!data.locked)
        iprComments.apply {
            compactNumberText(data.commentCount)
            goneFlag(data.commentCount)
        }
    }
}


class RepoVhBinding(override val data: ShortRepoRowItem) :
    BindingItem<ViewRepoBinding>(data), BindingLayout<ViewRepoBinding> by Companion {

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewRepoBinding = ViewRepoBinding.inflate(layoutInflater, parent, false)

    override fun ViewRepoBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        repoName.text = data.gitNameAndOwner().name
        repoDesc.text = data.description
        repoDesc.goneFlag(data.description)
        repoStars.compactNumberText(data.stargazers.totalCount)
        repoForks.compactNumberText(data.forks.totalCount)
        repoPrs.compactNumberText(data.pullRequests.totalCount)
        repoLanguage.text = data.primaryLanguage?.name
        repoLanguage.languageColor(data.primaryLanguage?.color)
        repoDate.relativeDateText(data.pushedAt)
    }

    override fun ViewRepoBinding.unbindView(holder: ViewHolder) {
        unbind(
            repoName,
            repoDesc,
            repoStars,
            repoForks,
            repoIssues,
            repoPrs,
            repoLanguage,
            repoDate
        )
    }

    companion object : BindingLayout<ViewRepoBinding> {
        override val layoutRes: Int
            get() = R.layout.view_repo
    }
}

class SlimEntryVhBinding(override val data: SlimEntry) :
    BindingItem<ViewSlimEntryBinding>(data), BindingLayout<ViewSlimEntryBinding> by Companion {

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewSlimEntryBinding = ViewSlimEntryBinding.inflate(layoutInflater, parent, false)

    override fun ViewSlimEntryBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        slimIcon.setImageResource(data.icon)
        slimText.text = data.text
        slimIndicator.goneFlag(data.onClick)
    }

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

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewUserHeaderBinding = ViewUserHeaderBinding.inflate(layoutInflater, parent, false)

    override fun ViewUserHeaderBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        userHeaderAvatar.glide(data.avatarUrl)
        userHeaderFollowToggle.apply {
            setImageResource(if (data.isViewerIsFollowing) R.drawable.ic_person_remove else R.drawable.ic_person_add)
            goneFlag(!data.isViewerCanFollow)
        }
        userHeaderName.text = data.name ?: data.login
        userHeaderEmail.apply {
            text = data.email
            goneFlag(data.email)
        }
        userHeaderWeb.apply {
            text = data.websiteUrl?.toString()
            goneFlag(data.websiteUrl)
        }
        userHeaderLocation.apply {
            text = data.location
            goneFlag(data.location)
        }
        userHeaderDesc.apply {
            text = data.bio
            goneFlag(data.bio)
        }
    }

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
    BindingItem<ViewUserContributionsBinding>(data),
    BindingLayout<ViewUserContributionsBinding> by Companion {

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewUserContributionsBinding =
        ViewUserContributionsBinding.inflate(layoutInflater, parent, false)

    override fun ViewUserContributionsBinding.bindView(
        holder: ViewHolder,
        payloads: MutableList<Any>
    ) {
        userContributions.contributions = data.contributionsCollection.fragments.shortContributions
    }

    override fun ViewUserContributionsBinding.unbindView(holder: ViewHolder) {
        userContributions.contributions = null
    }

    companion object : BindingLayout<ViewUserContributionsBinding> {
        override val layoutRes: Int
            get() = R.layout.view_user_contributions
    }

}

class PathCrumbHomeVhBinding(override val data: GitObjectID?) :
    BindingItem<ViewPathCrumbBinding>(data), BindingLayout<ViewPathCrumbBinding> by Companion {
    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewPathCrumbBinding = ViewPathCrumbBinding.inflate(layoutInflater, parent, false)

    override fun ViewPathCrumbBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        // noop
    }

    override fun ViewPathCrumbBinding.unbindView(holder: ViewHolder) {
        // noop
    }

    companion object : BindingLayout<ViewPathCrumbBinding> {
        override val layoutRes: Int
            get() = R.layout.view_path_crumb
    }
}

class PathCrumbVhBinding(override val data: PathCrumb) :
    BindingItem<ViewPathCrumbBinding>(data), BindingLayout<ViewPathCrumbBinding> by Companion {

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewPathCrumbBinding = ViewPathCrumbBinding.inflate(layoutInflater, parent, false)

    override fun ViewPathCrumbBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        pathText.text = data.segment
        val adapter =
            root.getTag(R.id.fastadapter_item_adapter) as? FastAdapter<PathCrumbVhBinding> ?: return
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

class TreeEntryVhBinding(override val data: TreeEntryItem) :
    BindingItem<ViewTreeEntryBinding>(data), BindingLayout<ViewTreeEntryBinding> by Companion {

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewTreeEntryBinding = ViewTreeEntryBinding.inflate(layoutInflater, parent, false)

    override fun ViewTreeEntryBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        treeEntryIcon.setImageResource(if (data.obj is ObjectItem.AsBlob) R.drawable.ic_file else R.drawable.ic_folder)
        treeEntryText.text = data.name
        treeEntrySize.treeEntrySizeText(data)
    }

    override fun ViewTreeEntryBinding.unbindView(holder: ViewHolder) {
        unbind(treeEntryIcon)
        unbind(treeEntryText, treeEntrySize)
    }

    companion object : BindingLayout<ViewTreeEntryBinding> {

        override val layoutRes: Int
            get() = R.layout.view_tree_entry
    }
}

class IssueCommentVhBinding(override val data: ShortIssueComment) :
    BindingItem<ViewIssueCommentBinding>(data),
    BindingLayout<ViewIssueCommentBinding> by Companion {

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewIssueCommentBinding = ViewIssueCommentBinding.inflate(layoutInflater, parent, false)

    override fun ViewIssueCommentBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        avatar.glideRound(data.fragments.shortComment?.author?.fragments?.shortActor?.avatarUrl)
        login.text = data.fragments.shortComment?.author?.fragments?.shortActor?.login
        date.relativeDateText(data.fragments.shortComment?.updatedAt)
        reaction.goneFlag(data.fragments.shortReaction?.isViewerCanReact != true)
        label.authorAssociation(data.fragments.shortComment?.authorAssociation)
        content.text = data.fragments.shortComment?.bodyText
    }

    override fun ViewIssueCommentBinding.unbindView(holder: ViewHolder) {
        unbind(avatar)
        unbind(login, date, content, label)
    }

    companion object : BindingLayout<ViewIssueCommentBinding> {

        override val layoutRes: Int
            get() = R.layout.view_issue_comment

    }
}

class PlaceholderVhBinding(@StringRes override val data: Int) :
    BindingItem<ViewPlaceholderBinding>(data), BindingLayout<ViewPlaceholderBinding> by Companion {

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewPlaceholderBinding = ViewPlaceholderBinding.inflate(layoutInflater, parent, false)

    override fun ViewPlaceholderBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        placeholderText.setText(data)
    }

    override fun ViewPlaceholderBinding.unbindView(holder: ViewHolder) {
        unbind(placeholderText)
    }

    companion object : BindingLayout<ViewPlaceholderBinding> {

        override val layoutRes: Int
            get() = R.layout.view_placeholder

    }
}

class RefEntryVhBinding(override val data: RefEntry) :
    BindingItem<ViewRefEntryBinding>(data), BindingLayout<ViewRefEntryBinding> by Companion {

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewRefEntryBinding = ViewRefEntryBinding.inflate(layoutInflater, parent, false)

    override fun ViewRefEntryBinding.bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        refText.apply {
            setCompoundDrawablesRelative(context.drawable(data.icon), null, null, null)
            text = data.ref.name
        }
    }

    override fun ViewRefEntryBinding.unbindView(holder: ViewHolder) {
        refText.setCompoundDrawablesRelative(null, null, null, null)
        unbind(refText)
    }

    companion object : BindingLayout<ViewRefEntryBinding> {

        override val layoutRes: Int
            get() = R.layout.view_ref_entry

    }
}

class RepoOverviewHeaderVhBinding(override val data: ShortRepoRowItem) :
    BindingItem<ViewRepoOverviewHeaderBinding>(data),
    BindingLayout<ViewRepoOverviewHeaderBinding> by Companion {

    override fun createBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): ViewRepoOverviewHeaderBinding =
        ViewRepoOverviewHeaderBinding.inflate(layoutInflater, parent, false)

    override fun ViewRepoOverviewHeaderBinding.bindView(
        holder: ViewHolder,
        payloads: MutableList<Any>
    ) {
        repoTitle.repoHeaderText(data)
        repoDesc.text = data.description
    }

    override fun ViewRepoOverviewHeaderBinding.unbindView(holder: ViewHolder) {
        unbind(repoTitle, repoDesc)
    }

    companion object : BindingLayout<ViewRepoOverviewHeaderBinding> {

        override val layoutRes: Int
            get() = R.layout.view_repo_overview_header

    }
}