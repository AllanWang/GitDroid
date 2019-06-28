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

//class GitCallMap<T, R>(val gitCall: GitCall<T>, val mapper: (T)-> R): GitCall<R> {
//
//}

fun <T, R> GitCall<T>.fmap(action: (T) -> R?): GitCall<R> = object : GitCall<R> {
    override suspend fun call(forceRefresh: Boolean): Response<R> = this@fmap.call(forceRefresh).fmap(action)
}

fun <T, R : Any> GitCall<List<T>>.lmap(action: (T) -> R?): GitCall<List<R>> = object : GitCall<List<R>> {
    override suspend fun call(forceRefresh: Boolean): Response<List<R>> =
        this@lmap.call(forceRefresh).fmap { list -> list.mapNotNull(action) }
}

data class GitCallResponse<T>(
    val operation: Operation<*, *, *>,
    val data: T,
    val errors: List<Error>,
    val dependentKeys: Set<String>,
    val fromCache: Boolean
)

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

    suspend fun searchUserIssues(
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

    suspend fun getIssue(repo: GitNameAndOwner, issueNumber: Int): GitCall<FullIssue> =
        query(GetIssueQuery(repo.owner, repo.name, issueNumber)) {
            repository?.issue?.fragments?.fullIssue
        }

    suspend fun searchUserRepos(
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

    suspend fun searchRepos(
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


    suspend fun getRepo(repo: GitNameAndOwner): GitCall<FullRepo> =
        query(GetRepoQuery(repo.owner, repo.name)) {
            repository?.fragments?.fullRepo
        }


    suspend fun getRepoObject(repo: GitNameAndOwner, oid: GitObjectID?): GitCall<ObjectItem> =
        if (oid == null) {
            query(GetRepoDefaultObjectQuery(repo.owner, repo.name)) {
                repository?.defaultBranchRef?.target?.fragments?.objectItem
            }
        } else {
            query(GetRepoObjectQuery(repo.owner, repo.name, oid)) {
                repository?.obj?.fragments?.objectItem
            }
        }


    suspend fun searchPullRequests(
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

    suspend fun getRefs(
        repo: GitNameAndOwner,
        branchCursor: String? = null,
        getBranches: Boolean = false,
        tagCursor: String? = null,
        getTags: Boolean = false
    ): GitCall<GitRefs> =
        query(
            GetRefsQuery(
                repo.owner,
                repo.name,
                Input.optional(branchCursor),
                getBranches,
                Input.optional(tagCursor),
                getTags
            )
        ) {
            val repository = repository ?: return@query null
            val branches = repository.branches?.nodes?.map { it.fragments.shortRef } ?: emptyList()
            val newBranchCursor = repository.branches?.pageInfo?.fragments?.shortPageInfo?.startCursor ?: branchCursor
            val tags = repository.tags?.nodes?.map { it.fragments.shortRef } ?: emptyList()
            val newTagCursor = repository.tags?.pageInfo?.fragments?.shortPageInfo?.startCursor ?: tagCursor
            GitRefs(branches, newBranchCursor, tags, newTagCursor)
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
