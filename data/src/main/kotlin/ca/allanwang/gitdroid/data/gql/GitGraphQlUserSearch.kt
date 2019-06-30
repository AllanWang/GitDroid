package ca.allanwang.gitdroid.data.gql

import com.apollographql.apollo.api.Input
import github.SearchIssuesQuery
import github.SearchUserReposQuery
import github.fragment.ShortIssueRowItem
import github.fragment.ShortRepoRowItem

 interface GitGraphQlUserSearch : GitGraphQlBase {

    fun searchUserIssues(
        login: String,
        count: Int = GQL_GET_COUNT,
        cursor: String? = null
    ): GitCall<List<ShortIssueRowItem>> =
        queryList(
            SearchIssuesQuery(
                login,
                Input.optional(count), Input.optional(cursor)
            )
        ) {
            search.nodes?.mapNotNull { it.fragments.shortIssueRowItem }
        }

    fun searchUserRepos(
        login: String,
        count: Int = GQL_GET_COUNT,
        cursor: String? = null
    ): GitCall<List<ShortRepoRowItem>> =
        queryList(
            SearchUserReposQuery(
                login,
                Input.optional(count),
                Input.optional(cursor)
            )
        ) {
            user?.repositories?.nodes?.mapNotNull { it.fragments.shortRepoRowItem }
        }
}