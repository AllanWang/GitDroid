package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.helpers.GitComparators
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.gitdroid.utils.setCoordinatorLayoutScrollingBehaviour
import ca.allanwang.gitdroid.views.FastBindingAdapter
import ca.allanwang.gitdroid.views.PathCrumb
import ca.allanwang.gitdroid.views.custom.PathCrumbsView
import ca.allanwang.gitdroid.views.databinding.ViewRepoFilesBinding
import ca.allanwang.gitdroid.views.item.TreeEntryVhBinding
import ca.allanwang.gitdroid.views.item.GenericBindingItem
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.kau.utils.startActivity
import github.fragment.FullRepo
import github.fragment.ObjectItem
import github.fragment.TreeEntryItem
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RepoActivity : ToolbarActivity<ViewRepoFilesBinding>() {

    override val layoutRes: Int
        get() = R.layout.view_repo_files

    val query by stringExtra(ARG_QUERY)

    private val pathCrumbs: PathCrumbsView
        get() = binding.repoPathCrumbs

    private val treeAdapter: FastBindingAdapter = FastBindingAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.root.setCoordinatorLayoutScrollingBehaviour()

        treeAdapter.onClickListener = { v, adapter, item, position ->
            if (item is TreeEntryVhBinding) {
                onClick(item.data)
                true
            } else {
                false
            }
        }

        binding.repoRecycler.adapter = treeAdapter

        pathCrumbs.callback = { data ->
            load(data, false)
        }
        binding.repoRefresh.setOnRefreshListener {
            load(pathCrumbs.getCurrentCrumb(), true)
        }
        loadRepo()
    }

    @Parcelize
    private data class InstanceState(val crumbs: List<PathCrumb>) : Parcelable

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (pathCrumbs.getCrumbs().isNotEmpty()) {
            return
        }
        if (!restoreFolder(savedInstanceState)) {
            loadRepo()
        }
    }

    private fun restoreFolder(savedInstanceState: Bundle): Boolean {
        val state: InstanceState = savedInstanceState.getParcelable(SAVED_STATE) ?: return false
        val crumbs = state.crumbs
        if (crumbs.isEmpty()) {
            return false
        }
        pathCrumbs.setCrumbs(state.crumbs)
        loadFolder(state.crumbs.last().oid)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val crumbs = pathCrumbs.getCrumbs()
        val state = InstanceState(crumbs)
        outState.putParcelable(SAVED_STATE, state)
    }

    private suspend fun showEntries(entries: List<TreeEntryItem>) {
        val sorted: List<GenericBindingItem> = withContext(Dispatchers.Default) {
            entries.sortedWith(GitComparators.treeEntryItem()).map { it.vh() }
        }
        withContext(Dispatchers.Main) {
            binding.repoRefresh.isRefreshing = false
            treeAdapter.add(sorted)
        }
    }

    private fun onClick(data: TreeEntryItem) {
        val obj = data.obj
        if (obj is TreeEntryItem.AsBlob) {
            if (obj.isBinary) {
                // todo
            } else {
                loadTextBlob(data.name, data.oid)
            }
        } else {
            pathCrumbs.addCrumb(PathCrumb(data.name, data.oid))
            loadFolder(data.oid)
        }
    }

    private fun load(data: PathCrumb?, forceRefresh: Boolean = false) {
        if (data == null) {
            loadRepo(forceRefresh)
        } else {
            loadFolder(data.oid, forceRefresh)
        }
    }

    private fun loadRepo(forceRefresh: Boolean = false) {
        treeAdapter.clear()
        launch {
            val repo = gdd.getRepo(query).await(forceRefresh = forceRefresh)
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

    private fun loadTextBlob(name: String, oid: GitObjectID, forceRefresh: Boolean = false) {
        BlobActivity.launch(this, query, name, oid)
    }

    private fun loadFolder(oid: GitObjectID, forceRefresh: Boolean = false) {
        treeAdapter.clear()
        L._d { "Loading folder $oid" }
        launch {
            val obj = gdd.getFileInfo(query, oid).await(forceRefresh = forceRefresh)
            if (obj !is ObjectItem.AsTree) {
                throw CancellationException(("Expected object to be tree, but actually ${obj.__typename}"))
            }

            val entries: List<TreeEntryItem> = obj.entries?.map { it.fragments.treeEntryItem } ?: emptyList()
            showEntries(entries)
        }
    }

    override fun finish() {
        BlobActivity.lexerCache.clear()
        super.finish()
    }

    override fun onBackPressed() {
        if (pathCrumbs.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    companion object {
        private const val ARG_QUERY = "arg_repo_query"

        private const val SAVED_STATE = "repo_saved_state"

        fun launch(context: Context, nameWithOwner: String) {
            context.startActivity<RepoActivity>(intentBuilder = {
                putExtra(ARG_QUERY, nameWithOwner)
            })
        }
    }
}