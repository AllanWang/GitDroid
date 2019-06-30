package ca.allanwang.gitdroid.data.gql

import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.GitRefs
import com.apollographql.apollo.api.Input
import github.*
import github.fragment.FullIssue
import github.fragment.FullRepo
import github.fragment.ObjectItem

interface GitGraphQlIssuePrs : GitGraphQlBase {
    fun getIssue(repo: GitNameAndOwner, issueNumber: Int): GitCall<FullIssue?> =
        query(GetIssueQuery(repo.owner, repo.name, issueNumber)) {
            it?.repository?.issue?.fragments?.fullIssue
        }


    fun getRepo(repo: GitNameAndOwner): GitCall<FullRepo?> =
        query(GetRepoQuery(repo.owner, repo.name)) {
            it?.repository?.fragments?.fullRepo
        }


    fun getRepoObject(repo: GitNameAndOwner, oid: GitObjectID?): GitCall<ObjectItem?> =
        if (oid == null) {
            query(GetRepoDefaultObjectQuery(repo.owner, repo.name)) {
                it?.repository?.defaultBranchRef?.target?.fragments?.objectItem
            }
        } else {
            query(GetRepoObjectQuery(repo.owner, repo.name, oid)) {
                it?.repository?.obj?.fragments?.objectItem
            }
        }

    fun getRefs(
        repo: GitNameAndOwner,
        branchCursor: String? = null,
        getBranches: Boolean = false,
        tagCursor: String? = null,
        getTags: Boolean = false
    ): GitCall<GitRefs?> =
        query(
            GetRefsQuery(
                repo.owner,
                repo.name,
                Input.optional(branchCursor),
                getBranches,
                Input.optional(tagCursor),
                getTags
            )
        ) { data ->
            val repository = data?.repository ?: return@query null
            val branches = repository.branches?.nodes?.map { it.fragments.shortRef } ?: emptyList()
            val newBranchCursor = repository.branches?.pageInfo?.fragments?.shortPageInfo?.startCursor ?: branchCursor
            val tags = repository.tags?.nodes?.map { it.fragments.shortRef } ?: emptyList()
            val newTagCursor = repository.tags?.pageInfo?.fragments?.shortPageInfo?.startCursor ?: tagCursor
            GitRefs(branches, newBranchCursor, tags, newTagCursor)
        }
}