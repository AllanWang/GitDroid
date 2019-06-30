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

fun ShortIssueRowItem.gitNameAndOwner(): GitNameAndOwner = repository.fragments.repoNameAndOwner.gitNameAndOwner()
fun ShortPullRequestRowItem.gitNameAndOwner(): GitNameAndOwner = repository.fragments.repoNameAndOwner.gitNameAndOwner()
fun ShortRepoRowItem.gitNameAndOwner(): GitNameAndOwner = fragments.repoNameAndOwner.gitNameAndOwner()
fun RepoNameAndOwner.gitNameAndOwner(): GitNameAndOwner =
    GitNameAndOwner(name = name, owner = owner.login)

data class GitRefs(
    val branchRefs: List<ShortRef>,
    val branchCursor: String?,
    val tagRefs: List<ShortRef>,
    val tagCursor: String?
)

@Parcelize
data class GitRef(val name: String, val prefix: String, val oid: GitObjectID): Parcelable

fun ShortRef.gitRef(): GitRef = GitRef(name, prefix, target.oid)
fun ShortRepoRowItem.gitRef(): GitRef? = defaultBranchRef?.fragments?.shortRef?.gitRef()