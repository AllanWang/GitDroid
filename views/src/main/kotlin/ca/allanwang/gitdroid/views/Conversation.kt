package ca.allanwang.gitdroid.views

import github.SearchIssuesQuery
import github.fragment.ShortIssueRowItem

data class ConversationModel(
    val avatarUrl: String,
    val user: String,
    val title: String,
    val subContent: String,
    val commentCount: Int,
    val labelColors: List<Int>,
    val time: String,
    val t: ShortIssueRowItem
)