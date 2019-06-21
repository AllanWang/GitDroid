package ca.allanwang.gitdroid.data

import com.squareup.moshi.Json

data class GitAccessToken(
    @Json(name = "access_token") val token: String,
    val scope: String, @Json(name = "token_type") val type: String
)

data class GitObjectID(val oid: String)