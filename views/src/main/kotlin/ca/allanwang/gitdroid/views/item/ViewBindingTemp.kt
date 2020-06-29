package ca.allanwang.gitdroid.views.item

import ca.allanwang.gitdroid.views.R
import ca.allanwang.gitdroid.views.databinding.*
import ca.allanwang.gitdroid.views.utils.*
import ca.allanwang.kau.utils.drawable
import github.GetProfileQuery
import github.fragment.*

fun ViewUserHeaderBinding.setModel(model: GetProfileQuery.User) {
    userHeaderAvatar.glide(model.avatarUrl)
    userHeaderFollowToggle.apply {
        setImageResource(if (model.isViewerIsFollowing) R.drawable.ic_person_remove else R.drawable.ic_person_add)
        goneFlag(!model.isViewerCanFollow)
    }
    userHeaderName.text = model.name ?: model.login
    userHeaderEmail.apply {
        text = model.email
        goneFlag(model.email)
    }
    userHeaderWeb.apply {
        text = model.websiteUrl?.toString()
        goneFlag(model.websiteUrl)
    }
    userHeaderLocation.apply {
        text = model.location
        goneFlag(model.location)
    }
    userHeaderDesc.apply {
        text = model.bio
        goneFlag(model.bio)
    }
}

fun ViewRepoOrigBinding.setModel(model: ShortRepoRowItem) {
    repoName.text = model.fragments.repoNameAndOwner.name
    repoDesc.apply {
        text = model.description
        goneFlag(model.description)
    }
    repoStars.compactNumberText(model.stargazers.totalCount)
    repoForks.compactNumberText(model.forkCount)
    repoIssues.compactNumberText(model.issues.totalCount)
    repoPrs.compactNumberText(model.pullRequests.totalCount)
    repoLanguage.apply {
        text = model.primaryLanguage?.name
        languageColor(model.primaryLanguage?.color)
    }
    repoDate.relativeDateText(model.pushedAt)
}

fun ViewUserContributionsBinding.setModel(model: ShortContributions) {
    userContributions.contributions = model
}

fun ViewRepoBinding.setModel(model: ShortRepoRowItem) {
    repoName.text = model.fragments.repoNameAndOwner.name
    repoDesc.apply {
        text = model.description
        goneFlag(model.description)
    }
    repoStars.compactNumberText(model.stargazers.totalCount)
    repoForks.compactNumberText(model.forkCount)
    repoIssues.compactNumberText(model.issues.totalCount)
    repoPrs.compactNumberText(model.pullRequests.totalCount)
    repoLanguage.apply {
        text = model.primaryLanguage?.name
        invisibleFlag(model.primaryLanguage?.name)
        languageColor(model.primaryLanguage?.color)
    }
    repoDate.relativeDateText(model.pushedAt)
}

fun ViewRepoOverviewHeaderBinding.setModel(model: ShortRepoRowItem) {
    repoTitle.repoHeaderText(model)
    repoDesc.text = model.description
}

fun ViewIssueOrPrItemBinding.setModel(model: GitIssueOrPr) {
    iprAvatar.glideRound(model.avatarUrl)
    iprLogin.text = model.login
    iprDate.relativeDateText(model.createdAt)
    iprTitle.text = model.title
    iprDetails.text = "${model.nameWithOwner}#${model.number}"
    iprLocked.goneFlag(!model.locked)
    iprComments.apply {
        compactNumberText(model.commentCount)
        goneFlag(model.commentCount)
    }
}

fun ViewPathCrumbBinding.setModel(model: PathCrumb) {
    pathText.text = model.segment
}

fun ViewRefEntryBinding.setModel(model: RefEntry) {
    refText.apply {
        setCompoundDrawablesRelative(context.drawable(model.icon), null, null, null)
        text = model.ref.name
    }
}

fun ViewMainIssueCommentBinding.setModel(model: ShortIssueComment) {
    avatar.glideRound(model.fragments.shortComment?.author?.fragments?.shortActor?.avatarUrl)
    login.text = model.fragments.shortComment?.author?.fragments?.shortActor?.login
    date.relativeDateText(model.fragments.shortComment?.updatedAt)
    content.text = model.fragments.shortComment?.bodyText
}

fun ViewSlimEntryBinding.setModel(model: SlimEntry) {
    slimIcon.setImageResource(model.icon)
    slimText.text = model.text
    slimIndicator.goneFlag(model.onClick)
}

fun ViewIssueCommentBinding.setModel(model: ShortIssueComment) {
    avatar.glideRound(model.fragments.shortComment?.author?.fragments?.shortActor?.avatarUrl)
    login.text = model.fragments.shortComment?.author?.fragments?.shortActor?.login
    date.relativeDateText(model.fragments.shortComment?.updatedAt)
    reaction.goneFlag(model.fragments.shortReaction?.isViewerCanReact != true)
    label.authorAssociation(model.fragments.shortComment?.authorAssociation)
    content.text = model.fragments.shortComment?.bodyText
}

fun ViewTreeEntryBinding.setModel(model: TreeEntryItem) {
    treeEntryIcon.setImageResource(if (model.obj is ObjectItem.AsBlob) R.drawable.ic_file else R.drawable.ic_folder)
    treeEntryText.text = model.name
    treeEntrySize.treeEntrySizeText(model)
}