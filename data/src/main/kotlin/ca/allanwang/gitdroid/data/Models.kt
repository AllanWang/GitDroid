package ca.allanwang.gitdroid.data

import android.os.Parcelable
import com.squareup.moshi.Json
import github.fragment.*
import kotlinx.android.parcel.Parcelize

data class GitAccessToken(
    @Json(name = "access_token") val token: String,
    val scope: String, @Json(name = "token_type") val type: String
)

@Parcelize
data class GitObjectID(val oid: String) : Parcelable


@Parcelize
data class GitNameAndOwner(val name: String, val owner: String) : Parcelable {
    val nameWithOwner: String
        get() = "$owner/$name"
}

fun ShortIssueRowItem.nameAndOwner(): GitNameAndOwner = repository.fragments.repoNameAndOwner.nameAndOwner()
fun ShortPullRequestRowItem.nameAndOwner(): GitNameAndOwner = repository.fragments.repoNameAndOwner.nameAndOwner()
fun ShortRepoRowItem.nameAndOwner(): GitNameAndOwner = fragments.repoNameAndOwner.nameAndOwner()
fun RepoNameAndOwner.nameAndOwner(): GitNameAndOwner =
    GitNameAndOwner(name = name, owner = owner.login)

data class GitRefs(
    val branchRefs: List<ShortRef>,
    val branchCursor: String?,
    val tagRefs: List<ShortRef>,
    val tagCursor: String?
)