package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.LoadingActivity
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.helpers.GitComparators
import ca.allanwang.gitdroid.databinding.ActivityRepoBinding
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.gitdroid.utils.setCoordinatorLayoutScrollingBehaviour
import ca.allanwang.gitdroid.views.*
import ca.allanwang.gitdroid.views.custom.PathCrumbsView
import ca.allanwang.kau.utils.startActivity
import github.fragment.FullRepo
import github.fragment.ObjectItem
import github.fragment.TreeEntryItem
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RepoActivity : LoadingActivity<ActivityRepoBinding>() {

    override val layoutRes: Int
        get() = R.layout.activity_repo

    val query by stringExtra(ARG_QUERY)

    private val pathCrumbs: PathCrumbsView
        get() = binding.repoFiles.repoPathCrumbs

    lateinit var treeAdapter: Adapter

    override fun onCreate2(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.also {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        binding.repoFiles.root.setCoordinatorLayoutScrollingBehaviour()

        treeAdapter = Adapter.bind(binding.repoFiles.repoRecycler).apply {
            onClick = { vhb, _, _ ->
                if (vhb is TreeEntryVhBinding) {
                    onClick(vhb.data)
                    true
                } else {
                    false
                }
            }
        }
        pathCrumbs.callback = { data, info ->
            load(data, info, false)
        }
        binding.repoFiles.repoRefresh.setOnRefreshListener {
            load(pathCrumbs.getCurrentCrumb(), null, true)
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
        val sorted: List<VHBindingType> = withContext(Dispatchers.Default) {
            entries.sortedWith(GitComparators.treeEntryItem()).map { it.vh() }
        }
        withContext(Dispatchers.Main) {
            binding.repoFiles.repoRefresh.isRefreshing = false
            treeAdapter.data = sorted
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

    private fun load(data: PathCrumb?, info: ClickInfo?, forceRefresh: Boolean = false) {
        if (info?.isLast != true) {
            if (data == null) {
                loadRepo(forceRefresh)
            } else {
                loadFolder(data.oid, forceRefresh)
            }
        }
    }

    private fun loadRepo(forceRefresh: Boolean = false) {
        treeAdapter.data = emptyList()
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
        BlobActivity.launch(this, query, name.substringAfter(".", ""), oid)
    }

    private fun loadFolder(oid: GitObjectID, forceRefresh: Boolean = false) {
        treeAdapter.data = emptyList()
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