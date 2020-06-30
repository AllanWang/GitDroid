package ca.allanwang.gitdroid.views.item

import ca.allanwang.gitdroid.views.databinding.ViewMainIssueCommentBinding
import ca.allanwang.gitdroid.views.databinding.ViewRepoBinding
import ca.allanwang.gitdroid.views.databinding.ViewRepoOrigBinding
import ca.allanwang.gitdroid.views.utils.*
import github.fragment.ShortIssueComment
import github.fragment.ShortRepoRowItem

fun ViewRepoOrigBinding.setModel(data: ShortRepoRowItem) {
    repoName.text = data.fragments.repoNameAndOwner.name
    repoDesc.apply {
        text = data.description
        goneFlag(data.description)
    }
    repoStars.compactNumberText(data.stargazers.totalCount)
    repoForks.compactNumberText(data.forkCount)
    repoIssues.compactNumberText(data.issues.totalCount)
    repoPrs.compactNumberText(data.pullRequests.totalCount)
    repoLanguage.apply {
        text = data.primaryLanguage?.name
        languageColor(data.primaryLanguage?.color)
    }
    repoDate.relativeDateText(data.pushedAt)
}

fun ViewRepoBinding.setModel(data: ShortRepoRowItem) {
    repoName.text = data.fragments.repoNameAndOwner.name
    repoDesc.apply {
        text = data.description
        goneFlag(data.description)
    }
    repoStars.compactNumberText(data.stargazers.totalCount)
    repoForks.compactNumberText(data.forkCount)
    repoIssues.compactNumberText(data.issues.totalCount)
    repoPrs.compactNumberText(data.pullRequests.totalCount)
    repoLanguage.apply {
        text = data.primaryLanguage?.name
        invisibleFlag(data.primaryLanguage?.name)
        languageColor(data.primaryLanguage?.color)
    }
    repoDate.relativeDateText(data.pushedAt)
}

fun ViewMainIssueCommentBinding.setModel(data: ShortIssueComment) {
    avatar.glideRound(data.fragments.shortComment?.author?.fragments?.shortActor?.avatarUrl)
    login.text = data.fragments.shortComment?.author?.fragments?.shortActor?.login
    date.relativeDateText(data.fragments.shortComment?.updatedAt)
    content.text = data.fragments.shortComment?.bodyText
}
