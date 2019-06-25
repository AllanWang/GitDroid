package ca.allanwang.gitdroid.data

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

interface GitCall<T> {
    suspend fun call(forceRefresh: Boolean = false): Response<T>
}

fun <T, R> GitCall<T>.fmap(action: (T) -> R?): GitCall<R> = object : GitCall<R> {
    override suspend fun call(forceRefresh: Boolean): Response<R> = this@fmap.call(forceRefresh).fmap(action)
}

fun <T, R : Any> GitCall<List<T>>.lmap(action: (T) -> R?): GitCall<List<R>> = object : GitCall<List<R>> {
    override suspend fun call(forceRefresh: Boolean): Response<List<R>> =
        this@lmap.call(forceRefresh).fmap { list -> list.mapNotNull(action) }
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
            override suspend fun call(forceRefresh: Boolean): Response<T> = withContext(Dispatchers.IO) {
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
            override suspend fun call(forceRefresh: Boolean): Response<R> = withContext(Dispatchers.IO) {
                q.policy(forceRefresh).toDeferred().await().fmap(mapper)
            }
        }
    }

    suspend fun me(): GitCall<MeQuery.Data> = query(MeQuery())

    suspend fun getProfile(login: String): GitCall<GetProfileQuery.User> =
        query(GetProfileQuery(login)) {
            user
        }

    suspend fun getUserIssues(
        login: String,
        count: Int = GET_COUNT,
        cursor: String? = null
    ): GitCall<List<ShortIssueRowItem>> =
        query(
            SearchIssuesQuery(
                login,
                Input.optional(count), Input.optional(cursor)
            )
        ) {
            search.nodes?.mapNotNull { it.fragments.shortIssueRowItem }
        }

    suspend fun getIssue(login: String, repo: String, issueNumber: Int): GitCall<FullIssue> =
        query(GetIssueQuery(login, repo, issueNumber)) {
            repository?.issue?.fragments?.fullIssue
        }

    suspend fun getUserRepos(
        login: String,
        count: Int = GET_COUNT,
        cursor: String? = null
    ): GitCall<List<ShortRepoRowItem>> =
        query(
            SearchUserReposQuery(
                login,
                Input.optional(count),
                Input.optional(cursor)
            )
        ) {
            user?.repositories?.nodes?.mapNotNull { it.fragments.shortRepoRowItem }
        }

    suspend fun getRepos(
        query: String,
        count: Int = GET_COUNT,
        cursor: String? = null
    ): GitCall<List<ShortRepoRowItem>> =
        query(
            SearchReposQuery(
                query,
                Input.optional(count),
                Input.optional(cursor)
            )
        ) {
            search.nodes?.mapNotNull { it.fragments.shortRepoRowItem }
        }


    suspend fun getRepo(login: String, repo: String): GitCall<FullRepo> =
        query(GetRepoQuery(login, repo)) {
            repository?.fragments?.fullRepo
        }


    suspend fun getObject(login: String, repo: String, oid: GitObjectID): GitCall<ObjectItem> =
        query(GetObjectQuery(login, repo, oid)) {
            repository?.obj?.fragments?.objectItem
        }

    suspend fun getPullRequests(
        login: String,
        count: Int = GET_COUNT,
        cursor: String? = null
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
fun <T, R> Response<T>.fmap(action: (T) -> R?): Response<R> =
    Response.builder<R>(operation())
        .data(data()?.let { action(it) })
        .errors(errors())
        .dependentKeys(dependentKeys())
        .fromCache(fromCache())
        .build()
