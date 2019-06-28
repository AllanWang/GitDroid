package ca.allanwang.gitdroid.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ca.allanwang.gitdroid.activity.base.IntentActivity.Args.repo
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.GitRef
import github.fragment.ObjectItem
import github.fragment.TreeEntryItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class RepoViewModel : BaseViewModel() {

    val ref = MutableLiveData<GitRef>()

    val entries = LoadingListLiveData<TreeEntryItem>()

    fun loadFolder(oid: GitObjectID?) = gitCallLaunch(entries) {
        entries.value = Loading
        viewModelScope.launch {
            ref.value
            val obj = gdd.getRepoObject(repo, oid)
                .await(forceRefresh = forceRefresh)
            val entries: List<TreeEntryItem>? =
                when (obj) {
                    is ObjectItem.AsTree -> obj.entries?.map { it.fragments.treeEntryItem }
                    is ObjectItem.AsCommit -> obj.tree.entries?.map { it.fragments.treeEntryItem }
                    else -> throw CancellationException(("Expected object to be tree, but actually ${obj.__typename}"))
                }
        }
    }

}
