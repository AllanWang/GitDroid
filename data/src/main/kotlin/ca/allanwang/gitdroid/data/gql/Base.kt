package ca.allanwang.gitdroid.data.gql

import ca.allanwang.gitdroid.data.BuildConfig
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.concurrent.TimeUnit

internal const val GQL_GET_COUNT = 30

/**
 * Kotlin copy of [Response], but with the possibility of nonnull data.
 */
data class GitResponse<T>(
    val operation: Operation<*, *, *>,
    val data: T,
    val errors: List<Error>,
    val dependentKeys: Set<String>,
    val fromCache: Boolean
)

inline fun <T, R> Response<T>.fmap(action: (T?) -> R): GitResponse<R> =
    GitResponse(
        operation = operation,
        data = action(data),
        errors = errors ?: emptyList(),
        dependentKeys = dependentKeys,
        fromCache = fromCache
    )

inline fun <T, R> GitResponse<T>.fmap(action: (T) -> R): GitResponse<R> =
    GitResponse(
        operation = operation,
        data = action(data),
        errors = errors,
        dependentKeys = dependentKeys,
        fromCache = fromCache
    )


interface GitCall<T> {
    suspend fun call(forceRefresh: Boolean = false): GitResponse<T>
}

inline fun <T, R> GitCall<T>.fmap(crossinline action: (T) -> R): GitCall<R> = object :
    GitCall<R> {
    override suspend fun call(forceRefresh: Boolean): GitResponse<R> =
        this@fmap.call(forceRefresh).fmap(action)
}

inline fun <T, R> GitCall<List<T>>.lmap(crossinline action: (T) -> R): GitCall<List<R>> =
    fmap { list -> list.map(action) }

interface GitGraphQlBase {

    companion object : KoinComponent {
        internal val apollo: ApolloClient by inject()

        internal fun <T> ApolloQueryCall<T>.policy(forceRefresh: Boolean = false): ApolloQueryCall<T> {
            val policy = HttpCachePolicy.CACHE_FIRST.run {
                when {
                    forceRefresh -> expireAfter(1, TimeUnit.MINUTES).expireAfterRead()
                    BuildConfig.DEBUG -> expireAfter(
                        30,
                        TimeUnit.MINUTES
                    )
                    else -> expireAfter(10, TimeUnit.MINUTES)
                }
            }
            return httpCachePolicy(policy)
        }
    }

    fun <D : Operation.Data, T : Any, V : Operation.Variables>
            query(
        query: com.apollographql.apollo.api.Query<D, T, V>
    ): GitCall<T?> = query(query) { it }

    fun <D : Operation.Data, T : Any, V : Operation.Variables, R>
            query(
        query: com.apollographql.apollo.api.Query<D, T, V>,
        mapper: (T?) -> R
    ): GitCall<R> {
        val q = apollo.query(query)
        return object : GitCall<R> {
            override suspend fun call(forceRefresh: Boolean): GitResponse<R> =
                withContext(Dispatchers.IO) {
                    q.policy(forceRefresh).toDeferred().await().fmap(mapper)
                }
        }
    }

    fun <D : Operation.Data, T : Any, V : Operation.Variables, R>
            queryList(
        query: com.apollographql.apollo.api.Query<D, T, V>,
        mapper: T.() -> List<R>?
    ): GitCall<List<R>> {
        val q = apollo.query(query)
        return object : GitCall<List<R>> {
            override suspend fun call(forceRefresh: Boolean): GitResponse<List<R>> =
                withContext(Dispatchers.IO) {
                    q.policy(forceRefresh).toDeferred().await().fmap {
                        it?.mapper() ?: emptyList()
                    }
                }
        }
    }

}