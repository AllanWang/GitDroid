package ca.allanwang.gitdroid.data

import ca.allanwang.gitdroid.logger.L
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toDeferred
import github.*
import github.fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

private const val GET_COUNT = 30

interface GitCall<T : Any> {
    suspend fun call(forceRefresh: Boolean = false): Response<T?>
}

interface GitGraphQl {

    companion object : KoinComponent {
        internal val apollo: ApolloClient by inject()

        internal fun <T> ApolloQueryCall<T>.policy(forceRefresh: Boolean = false): ApolloQueryCall<T> {
            val policy = HttpCachePolicy.CACHE_FIRST.run {
                when {
                    forceRefresh -> expireAfter(1, TimeUnit.MINUTES).expireAfterRead()
                    BuildConfig.DEBUG -> expireAfter(30, TimeUnit.MINUTES)
                    else -> expireAfter(10, TimeUnit.MINUTES)
                }
            }
            return httpCachePolicy(policy)
        }
    }

    suspend fun <D : Operation.Data, T : Any, V : Operation.Variables>
            query(
        query: com.apollographql.apollo.api.Query<D, T, V>
    ): GitCall<T> {
        val q = apollo.query(query)
        return object : GitCall<T> {
            override suspend fun call(forceRefresh: Boolean): Response<T?> = withContext(Dispatchers.IO) {
                q.policy(forceRefresh).toDeferred().await()
            }
        }
    }

    suspend fun <D : Operation.Data, T : Any, V : Operation.Variables, R : Any>
            query(
        query: com.apollographql.apollo.api.Query<D, T, V>,
        mapper: T.() -> R?
    ): GitCall<R> {
        val q = apollo.query(query)
        return object : GitCall<R> {
            override suspend fun call(forceRefresh: Boolean): Response<R?> = withContext(Dispatchers.IO) {
                q.policy(forceRefresh).toDeferred().await().map(mapper)
            }
        }
    }

    suspend fun me(forceRefresh: Boolean = false): GitCall<MeQuery.Data> = query(MeQuery())

    suspend fun getProfile(login: String, forceRefresh: Boolean = false): GitCall<GetProfileQuery.User> =
        query(GetProfileQuery(login)) {
            user
        }

    suspend fun getIssues(
        login: String,
        count: Int = GET_COUNT,
        cursor: String? = null,
        forceRefresh: Boolean = false
    ): GitCall<List<ShortIssueRowItem>> =
        query(
            SearchIssuesQuery(
                login,
                Input.optional(count), Input.optional(cursor)
            )
        ) {
            search.nodes?.mapNotNull { it.fragments.shortIssueRowItem }
        }

    suspend fun getRepos(
        login: String,
        count: Int = GET_COUNT,
        cursor: String? = null,
        forceRefresh: Boolean = false
    ): GitCall<List<ShortRepoRowItem>> =
        query(
            SearchReposQuery(
                login,
                Input.optional(count), Input.optional(cursor)
            )
        ) {
            search.nodes?.mapNotNull { it.fragments.shortRepoRowItem }
        }

    suspend fun getRepo(
        query: String
    ): GitCall<FullRepo> =
        query(RepoInfoQuery(query)) {
            search.nodes?.firstOrNull()?.fragments?.fullRepo
        }

    suspend fun getFileInfo(query: String, oid: GitObjectID): GitCall<ObjectItem> =
        query(ObjectInfoQuery(query, oid)) {
//            L._d { "$oid ${this.toString().replace(",", "\n\t")}" }
            search.nodes?.firstOrNull()?.let { it as? ObjectInfoQuery.AsRepository }?.obj?.fragments?.objectItem
        }

    suspend fun getPullRequests(
        login: String,
        count: Int = GET_COUNT,
        cursor: String? = null,
        forceRefresh: Boolean = false
    ): GitCall<List<ShortPullRequestRowItem>> =
        query(
            SearchPullRequestsQuery(
                login,
                Input.optional(count), Input.optional(cursor)
            )
        ) {
            search.nodes?.mapNotNull { it.fragments.shortPullRequestRowItem }
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
