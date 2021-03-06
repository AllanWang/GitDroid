package ca.allanwang.gitdroid.viewmodel

import android.os.Bundle
import androidx.annotation.CheckResult
import ca.allanwang.gitdroid.activity.base.IntentActivity
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.GitRef
import ca.allanwang.gitdroid.data.gql.fmap
import ca.allanwang.gitdroid.data.helpers.GitComparators
import ca.allanwang.gitdroid.viewmodel.base.*
import github.fragment.FullRepo
import github.fragment.ObjectItem
import github.fragment.TreeEntryItem
import kotlinx.coroutines.CancellationException

class RepoViewModel : BaseViewModel() {

    val ref = MutableLiveDataKtx<GitRef?>()

    val repo = IntentLiveData<GitNameAndOwner>()

    val fullRepo = LoadingLiveData<FullRepo?>()

    val entries = LoadingListLiveData<TreeEntryItem>()

    override fun withBundle(bundle: Bundle) {
        repo.value = bundle.getParcelable(IntentActivity.Args.repo)!!
        ref.value = bundle.getParcelable(IntentActivity.Args.ref)
        fullRepoLoader().execute()
    }

    /**
     * Returns entries for specified [oid].
     * Result will be sorted in alphabetical order, with folders first. See [GitComparators.treeEntryItem]
     */
    @CheckResult
    fun entryLoader(oid: GitObjectID?) =
        gitCallLaunch(entries, gdd.getRepoObject(repo.value, oid).fmap { obj ->
            val result = when (obj) {
                is ObjectItem.AsTree -> obj.entries?.map { it.fragments.treeEntryItem }
                is ObjectItem.AsCommit -> obj.tree.entries?.map { it.fragments.treeEntryItem }
                else -> throw CancellationException(("Expected object to be tree, but actually ${obj?.__typename}"))
            } ?: emptyList()
            result.sortedWith(GitComparators.treeEntryItem())
        })

    fun fullRepoLoader() =
        gitCallLaunch(fullRepo, gdd.getRepo(repo.value))
}
