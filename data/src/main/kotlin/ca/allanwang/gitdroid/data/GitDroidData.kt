package ca.allanwang.gitdroid.data

import com.apollographql.apollo.ApolloClient
import okhttp3.OkHttpClient
import org.koin.core.KoinComponent
import org.koin.core.inject

interface TokenSupplier {
    fun getToken(): String?
}

object GitDroidData : KoinComponent {

    private val tokenSupplier: TokenSupplier by inject()

    const val GRAPHQL_URL = "https://api.github.com/graphql"

    val apollo: ApolloClient by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(AuthInterceptor("bearer", tokenSupplier))
            .build()

        ApolloClient.builder()
            .serverUrl(GRAPHQL_URL)
            .okHttpClient(okHttpClient)
            .build()
    }
}