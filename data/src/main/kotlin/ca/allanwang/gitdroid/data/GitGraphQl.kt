package ca.allanwang.gitdroid.data

import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import github.*
import github.fragment.ShortIssueRowItem
import github.fragment.ShortPullRequestRowItem
import github.fragment.ShortRepoRowItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

private const val GET_COUNT = 30

interface GitGraphQl {

    fun <T> ApolloQueryCall<T>.policy(forceRefresh: Boolean): ApolloQueryCall<T> {
        val policy = HttpCachePolicy.CACHE_FIRST.run {
            when {
                forceRefresh -> expireAfterRead()
                BuildConfig.DEBUG -> expireAfter(30, TimeUnit.MINUTES)
                else -> expireAfter(10, TimeUnit.MINUTES)
            }
        }
        return httpCachePolicy(policy)
    }

    suspend fun <D : Operation.Data, T, V : Operation.Variables>
            query(
        query: com.apollographql.apollo.api.Query<D, T, V>,
        forceRefresh: Boolean
    ): Response<T>

    suspend fun <D : Operation.Data, T, V : Operation.Variables, R>
            query(
        query: com.apollographql.apollo.api.Query<D, T, V>,
        forceRefresh: Boolean, mapper: T.() -> R
    ): Response<R> {
        val response = query(query, forceRefresh)
        return withContext(Dispatchers.Default) {
            response.map(mapper)
        }
    }

    suspend fun me(forceRefresh: Boolean = false) = query(MeQuery(), forceRefresh = forceRefresh)

    suspend fun getProfile(login: String, forceRefresh: Boolean = false): Response<GetProfileQuery.Data> =
        query(GetProfileQuery(login), forceRefresh = forceRefresh)

    suspend fun getIssues(
        login: String,
        count: Int = GET_COUNT,
        cursor: String? = null,
        forceRefresh: Boolean = false
    ): Response<List<ShortIssueRowItem>> =
        query(
            SearchIssuesQuery(
                login,
                Input.optional(count), Input.optional(cursor)
            ), forceRefresh = forceRefresh
        ) {
            search.nodes?.mapNotNull { it.fragments.shortIssueRowItem } ?: emptyList()
        }

    suspend fun getRepos(
        login: String,
        count: Int = GET_COUNT,
        cursor: String? = null,
        forceRefresh: Boolean = false
    ): Response<List<ShortRepoRowItem>> =
        query(
            SearchReposQuery(
                login,
                Input.optional(count), Input.optional(cursor)
            ), forceRefresh = forceRefresh
        ) {
            search.nodes?.mapNotNull { it.fragments.shortRepoRowItem } ?: emptyList()
        }

    suspend fun getPullRequests(
        login: String,
        count: Int = GET_COUNT,
        cursor: String? = null,
        forceRefresh: Boolean = false
    ): Response<List<ShortPullRequestRowItem>> =
        query(
            SearchPullRequestsQuery(
                login,
                Input.optional(count), Input.optional(cursor)
            ),
            forceRefresh = forceRefresh
        ) {
            search.nodes?.mapNotNull { it.fragments.shortPullRequestRowItem } ?: emptyList()
        }
}

/**
 * Converts a response's data from one form to another
 */
fun <T, U> Response<T>.map(action: (T) -> U): Response<U> =
    Response.builder<U>(operation())
        .data(data()?.let { action(it) })
        .errors(errors())
        .dependentKeys(dependentKeys())
        .fromCache(fromCache())
        .build()
