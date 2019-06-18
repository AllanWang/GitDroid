package ca.allanwang.gitdroid.data

import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import github.GetProfileQuery
import github.MeQuery
import github.SearchIssuesQuery
import github.fragment.ShortIssueRowItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// TODO replace with 30
private const val GET_COUNT = 5

interface GitGraphQl {
    suspend fun <D : Operation.Data, T, V : Operation.Variables>
            query(query: com.apollographql.apollo.api.Query<D, T, V>): Response<T>

    suspend fun <D : Operation.Data, T, V : Operation.Variables, R>
            query(query: com.apollographql.apollo.api.Query<D, T, V>, mapper: T.() -> R): Response<R> {
        val response = query(query)
        return withContext(Dispatchers.Default) {
            response.map(mapper)
        }
    }

    suspend fun me() = query(MeQuery())

    suspend fun getProfile(login: String): Response<GetProfileQuery.Data> = query(GetProfileQuery(login))

    suspend fun getIssues(
        login: String,
        count: Int = GET_COUNT,
        cursor: String? = null
    ): Response<List<ShortIssueRowItem>> =
        query(SearchIssuesQuery(login, Input.optional(count), Input.optional(cursor))) {
            search.nodes?.mapNotNull { it.fragments.shortIssueRowItem } ?: emptyList()
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
