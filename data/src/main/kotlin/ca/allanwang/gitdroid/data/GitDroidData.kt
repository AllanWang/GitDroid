package ca.allanwang.gitdroid.data

import ca.allanwang.gitdroid.data.helpers.AuthInterceptor
import ca.allanwang.gitdroid.data.helpers.DateApolloAdapter
import ca.allanwang.gitdroid.data.helpers.ObjectApolloAdapter
import ca.allanwang.gitdroid.data.helpers.UriApolloAdapter
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toDeferred
import github.type.CustomType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.koin.core.KoinComponent
import org.koin.core.inject

interface TokenSupplier {
    fun getToken(): String?
}

object GitDroidData : KoinComponent {

    private val tokenSupplier: TokenSupplier by inject()

    /**
     * General references
     * https://developer.github.com/v4/
     */
    const val GRAPHQL_URL = "https://api.github.com/graphql"

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

    suspend fun <D : Operation.Data, T, V : Operation.Variables> query(query: Query<D, T, V>): Response<T> =
        withContext(Dispatchers.IO) {
            apollo.query(query).toDeferred().await()
        }
}