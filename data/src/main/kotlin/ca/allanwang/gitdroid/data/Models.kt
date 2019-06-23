package ca.allanwang.gitdroid.data

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

data class GitAccessToken(
    @Json(name = "access_token") val token: String,
    val scope: String, @Json(name = "token_type") val type: String
)

@Parcelize
data class GitObjectID(val oid: String) : Parcelable