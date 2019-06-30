package ca.allanwang.gitdroid.data.gql

import com.apollographql.apollo.api.Input
import github.SearchPullRequestsQuery
import github.SearchReposQuery
import github.fragment.ShortPullRequestRowItem
import github.fragment.ShortRepoRowItem

 interface GitGraphQlSearch  : GitGraphQlBase {
    fun searchRepos(
        query: String,
        count: Int = GQL_GET_COUNT,
        cursor: String? = null
    ): GitCall<List<ShortRepoRowItem>> =
        queryList(
            SearchReposQuery(
                query,
                Input.optional(count),
                Input.optional(cursor)
            )
        ) {
            search.nodes?.mapNotNull { it.fragments.shortRepoRowItem }
        }

    fun searchPullRequests(
        login: String,
        count: Int = GQL_GET_COUNT,
        cursor: String? = null
    ): GitCall<List<ShortPullRequestRowItem>> =
        queryList(
            SearchPullRequestsQuery(
                login,
                Input.optional(count), Input.optional(cursor)
            )
        ) {
            search.nodes?.mapNotNull { it.fragments.shortPullRequestRowItem }
        }

}