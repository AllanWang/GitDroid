package ca.allanwang.gitdroid.viewmodel

import androidx.lifecycle.MutableLiveData
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.GitRef
import ca.allanwang.gitdroid.data.fmap
import github.fragment.ObjectItem
import github.fragment.TreeEntryItem
import kotlinx.coroutines.CancellationException

class RepoViewModel : BaseViewModel() {

    val ref = MutableLiveData<GitRef>()

    val entries = LoadingListLiveData<TreeEntryItem>()

    fun loadFolder(repo: GitNameAndOwner, oid: GitObjectID?) =
        gitCallLaunch(entries, gdd.getRepoObject(repo, oid).fmap { obj ->
            when (obj) {
                is ObjectItem.AsTree -> obj.entries?.map { it.fragments.treeEntryItem }
                is ObjectItem.AsCommit -> obj.tree.entries?.map { it.fragments.treeEntryItem }
                else -> throw CancellationException(("Expected object to be tree, but actually ${obj?.__typename}"))
            } ?: emptyList()
        })

}
