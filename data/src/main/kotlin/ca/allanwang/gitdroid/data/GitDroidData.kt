package ca.allanwang.gitdroid.data

import android.content.Context
import ca.allanwang.gitdroid.data.GitGraphQl.Companion.apollo
import ca.allanwang.gitdroid.data.helpers.*
import ca.allanwang.gitdroid.data.helpers.DateTimeApolloAdapter
import ca.allanwang.gitdroid.data.helpers.ObjectApolloAdapter
import ca.allanwang.gitdroid.data.helpers.UriApolloAdapter
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.api.cache.http.HttpCache
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.cache.http.ApolloHttpCache
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.coroutines.toDeferred
import github.type.CustomType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File
import java.math.BigInteger
import java.security.SecureRandom


interface TokenSupplier {
    fun getToken(): String?
}

data class OAuthRequest(val url: String, val state: String) {
    override fun toString(): String = "OAuthRequest"
}

class GitDroidData : KoinComponent, GitGraphQl {

    companion object {
        internal const val API_BASE_URL = "https://api.github.com"

        internal const val BASE_URL = "https://github.com"

        /**
         * General references
         * https://developer.github.com/v4/
         */
        private const val GRAPHQL_URL = "$API_BASE_URL/graphql"

        private const val OAUTH_URL = "$BASE_URL/login/oauth/authorize"

        const val REDIRECT_URL = "gitdroid://login"

        const val CACHE_FILE = "gdd_cache_dir"

        fun module(context: Context?) = module {
            if (context != null) {
                single(named(CACHE_FILE)) {
                    File(context.applicationContext.cacheDir, "apolloCache")
                }
            }
            single<ApolloClient> {

                val tokenSupplier: TokenSupplier = get()
                val cacheFile: File? = getOrNull(named(CACHE_FILE))

                val cacheStore: HttpCache?

                if (cacheFile != null) {
                    val cacheSize = 1024L * 1024L
                    cacheStore = ApolloHttpCache(DiskLruHttpCacheStore(cacheFile, cacheSize))
                } else {
                    cacheStore = null
                }

                val okHttpClient = OkHttpClient.Builder()
                    .addNetworkInterceptor(AuthInterceptor("bearer", tokenSupplier))
                    .build()

                val builder = ApolloClient.builder()
                    .serverUrl(GRAPHQL_URL)
                    .okHttpClient(okHttpClient)
                    .addCustomTypeAdapter(CustomType.URI, UriApolloAdapter)
                    .addCustomTypeAdapter(CustomType.DATETIME, DateTimeApolloAdapter)
                    .addCustomTypeAdapter(CustomType.GITOBJECTID, GitObjectIDApolloAdapter)
                    .addCustomTypeAdapter(CustomType.HTML, ObjectApolloAdapter)

                if (cacheStore != null) {
                    builder.httpCache(cacheStore)
                }

                builder.build()
            }
            single { GitDroidData() }
        }
    }

    /**
     * See https://developer.github.com/apps/building-oauth-apps/authorizing-oauth-apps/
     */
    object Query {
        const val CODE = "code"
        const val STATE = "state"
        /**
         * See https://developer.github.com/apps/building-oauth-apps/understanding-scopes-for-oauth-apps/
         */
        const val SCOPE = "scope"
        const val SCOPE_VALUE = "user,repo,gist,notifications,read:org"
        const val REDIRECT_URI = "redirect_uri"
        const val CLIENT_ID = "client_id"
        const val CLIENT_SECRET = "client_secret"
    }

    /**
     * See https://developer.github.com/apps/building-oauth-apps/authorizing-oauth-apps/
     */
    fun oauthUrl(): OAuthRequest {
        val state = BigInteger(130, SecureRandom()).toString(32)
        val url = HttpUrl.parse(OAUTH_URL)!!.newBuilder()
            .addQueryParameter(Query.CLIENT_ID, BuildConfig.GITHUB_CLIENT_ID)
            .addQueryParameter(Query.REDIRECT_URI, REDIRECT_URL)
            .addQueryParameter(Query.STATE, state)
            .addQueryParameter(Query.SCOPE, Query.SCOPE_VALUE)
        return OAuthRequest(url.build().toString(), state)
    }

}