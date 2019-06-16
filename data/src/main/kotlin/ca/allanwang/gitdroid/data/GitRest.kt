package ca.allanwang.gitdroid.data

import ca.allanwang.gitdroid.data.helpers.JsonInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.POST
import retrofit2.http.Query

interface GitAuthRest {
    @POST("login/oauth/access_token")
    suspend fun accessToken(
        @Query(GitDroidData.Query.CODE) code: String,
        @Query(GitDroidData.Query.STATE) state: String? = null,
        @Query(GitDroidData.Query.CLIENT_ID) clientId: String = BuildConfig.GITHUB_CLIENT_ID,
        @Query(GitDroidData.Query.CLIENT_SECRET) clientSecret: String = BuildConfig.GITHUB_CLIENT_SECRET,
        @Query(GitDroidData.Query.REDIRECT_URI) redirectUri: String = GitDroidData.REDIRECT_URL
    ): GitAccessToken
}

val gitAuthRest: GitAuthRest = run {

    val client = OkHttpClient.Builder()
        .addInterceptor(JsonInterceptor())

    if (BuildConfig.DEBUG) {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        client.addInterceptor(logger)
    }

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())

    val retrofit = Retrofit.Builder()
        .baseUrl(GitDroidData.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi.build()).asLenient())
        .client(client.build())
        .build()

    retrofit.create(GitAuthRest::class.java)
}