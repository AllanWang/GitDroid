package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.helpers.GitComparators
import ca.allanwang.gitdroid.views.Adapter
import ca.allanwang.gitdroid.views.PathCrumb
import ca.allanwang.gitdroid.views.TreeEntryVhBinding
import ca.allanwang.gitdroid.views.custom.PathCrumbsView
import ca.allanwang.gitdroid.views.databinding.ViewRepoFilesBinding
import ca.allanwang.gitdroid.views.vh
import ca.allanwang.kau.utils.startActivity
import github.fragment.FullRepo
import github.fragment.ObjectItem
import github.fragment.TreeEntryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RepoActivity : LoadingActivity<ViewRepoFilesBinding>() {

    override val layoutRes: Int = R.layout.view_repo_files

    val query by stringExtra(ARG_QUERY)

    val pathCrumbs: PathCrumbsView
        get() = binding.repoPathCrumbs

    lateinit var treeAdapter: Adapter

    override fun onCreate2(savedInstanceState: Bundle?) {
        treeAdapter = Adapter.bind(binding.repoRecycler).apply {
            onClick = { vhb, view, info ->
                if (vhb !is TreeEntryVhBinding) {
                    false
                } else {
                    onClick(vhb.data)
                    true
                }
            }
        }
        pathCrumbs.callback = { data ->
            data.oid.also {
                if (it == null) {
                    loadRepo()
                } else {
                    loadFolder(it)
                }
            }
        }
        loadRepo()
    }

    private suspend fun showEntries(entries: List<TreeEntryItem>) {
        val sorted = withContext(Dispatchers.Default) {
            entries.sortedWith(GitComparators.treeEntryItem()).map { it.vh() }
        }
        withContext(Dispatchers.Main) {
            treeAdapter.data = sorted
        }
    }

    private fun onClick(data: TreeEntryItem) {
        pathCrumbs.addCrumb(PathCrumb(data.name, data.oid))
        treeAdapter.data = emptyList()
        if (data is TreeEntryItem.AsBlob) {
            if (data.isBinary) {
                // todo
            } else {
                loadTextBlob(data.oid)
            }
        } else {
            loadFolder(data.oid)
        }
    }

    private fun loadRepo() {
        launch {
            val repo = gdd.getRepo(query).await()
            val defaultBranch = repo.defaultBranchRef
            if (defaultBranch == null) {

            } else {
                val entries: List<TreeEntryItem> = defaultBranch
                    .target
                    .let { it as FullRepo.AsCommit }
                    .tree
                    .entries
                    ?.map { it.fragments.treeEntryItem }
                    ?: emptyList()

                showEntries(entries)
            }
        }
    }

    private fun loadTextBlob(oid: GitObjectID) {
        launch {
            val obj = gdd.getFileInfo(query, oid).await() as? ObjectItem.AsBlob ?: return@launch

        }
    }

    private fun loadFolder(oid: GitObjectID) {
        launch {
            val obj = gdd.getFileInfo(query, oid).await() as? ObjectItem.AsTree ?: return@launch
            val entries: List<TreeEntryItem> = obj.entries?.map { it.fragments.treeEntryItem } ?: emptyList()
            showEntries(entries)
        }
    }

    override fun onBackPressed() {
        if (pathCrumbs.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    companion object {
        private const val ARG_QUERY = "arg_query"

        fun launch(context: Context, nameWithOwner: String) {
            context.startActivity<RepoActivity>(intentBuilder = {
                putExtra(ARG_QUERY, nameWithOwner)
            })
        }
    }
}