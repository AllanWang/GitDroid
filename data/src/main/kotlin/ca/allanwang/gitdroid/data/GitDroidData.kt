package ca.allanwang.gitdroid.data

import ca.allanwang.gitdroid.data.helpers.AuthInterceptor
import ca.allanwang.gitdroid.data.helpers.DateApolloAdapter
import ca.allanwang.gitdroid.data.helpers.ObjectApolloAdapter
import ca.allanwang.gitdroid.data.helpers.UriApolloAdapter
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toDeferred
import github.type.CustomType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.math.BigInteger
import java.security.SecureRandom

interface TokenSupplier {
    fun getToken(): String?
}

data class OAuthRequest(val url: String, val state: String) {
    override fun toString(): String = "OAuthRequest"
}

object GitDroidData : KoinComponent {

    private val tokenSupplier: TokenSupplier by inject()

    internal const val API_BASE_URL = "https://api.github.com"

    internal const val BASE_URL = "https://github.com"

    /**
     * General references
     * https://developer.github.com/v4/
     */
    private const val GRAPHQL_URL = "$API_BASE_URL/graphql"

    private const val OAUTH_URL = "$BASE_URL/login/oauth/authorize"

    const val REDIRECT_URL = "gitdroid://login"

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

    private val apollo: ApolloClient by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(AuthInterceptor("bearer", tokenSupplier))
            .build()

        ApolloClient.builder()
            .serverUrl(GRAPHQL_URL)
            .okHttpClient(okHttpClient)
            .addCustomTypeAdapter(CustomType.URI, UriApolloAdapter)
            .addCustomTypeAdapter(CustomType.DATETIME, DateApolloAdapter)
            .addCustomTypeAdapter(CustomType.HTML, ObjectApolloAdapter)
            .build()
    }

    suspend fun <D : Operation.Data, T, V : Operation.Variables> query(query: com.apollographql.apollo.api.Query<D, T, V>): Response<T> =
        withContext(Dispatchers.IO) {
            apollo.query(query).toDeferred().await()
        }
}