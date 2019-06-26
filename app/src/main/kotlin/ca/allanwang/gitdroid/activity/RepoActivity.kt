package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.helpers.GitComparators
import ca.allanwang.gitdroid.utils.lazyUi
import ca.allanwang.gitdroid.views.FastBindingAdapter
import ca.allanwang.gitdroid.views.PathCrumb
import ca.allanwang.gitdroid.views.custom.PathCrumbsView
import ca.allanwang.gitdroid.views.databinding.ViewRepoFilesBinding
import ca.allanwang.gitdroid.views.entries
import ca.allanwang.gitdroid.views.item.GenericBindingItem
import ca.allanwang.gitdroid.views.item.RefEntryVhBinding
import ca.allanwang.gitdroid.views.item.TreeEntryVhBinding
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.startActivity
import com.afollestad.materialdialogs.list.customListAdapter
import github.fragment.FullRepo
import github.fragment.ObjectItem
import github.fragment.ShortRef
import github.fragment.TreeEntryItem
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RepoActivity : ToolbarActivity<ViewRepoFilesBinding>() {

    override val layoutRes: Int
        get() = R.layout.view_repo_files

    val repo by repoExtra()

    private var currentRef: ShortRef? = null

    private val pathCrumbs: PathCrumbsView
        get() = binding.repoPathCrumbs

    private val fastAdapter: FastBindingAdapter by lazyUi {
        FastBindingAdapter().apply {
            onClickListener = { _, _, item, _ ->
                if (item is TreeEntryVhBinding) {
                    onClick(item.data)
                    true
                } else {
                    false
                }
            }
        }
    }

    private val refAdapter: FastBindingAdapter by lazyUi {
        FastBindingAdapter().apply {
            onClickListener = { _, _, item, _ ->
                if (item is RefEntryVhBinding) {
                    currentRef = item.data.ref
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.repoRecycler.adapter = fastAdapter

        pathCrumbs.callback = { data ->
            loadObject(data, false)
        }
        binding.repoRefresh.setOnRefreshListener {
            loadObject(pathCrumbs.getCurrentCrumb(), true)
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
            fastAdapter.add(sorted)
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

    private fun loadObject(data: PathCrumb?, forceRefresh: Boolean = false) {
        if (data == null) {
            loadRepo(forceRefresh)
        } else {
            loadFolder(data.oid, forceRefresh)
        }
    }

    private fun loadRepo(forceRefresh: Boolean = false) {
        binding.repoRefresh.isRefreshing = false
        fastAdapter.clear()
        launch {
            val repo = gdd.getRepo(repo).await(forceRefresh = forceRefresh)
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
        BlobActivity.launch(this, repo, name, oid)
    }

    private fun loadFolder(oid: GitObjectID, forceRefresh: Boolean = false) {
        binding.repoRefresh.isRefreshing = false
        fastAdapter.clear()
        launch {
            val obj = gdd.getObject(repo, oid).await(forceRefresh = forceRefresh)
            if (obj !is ObjectItem.AsTree) {
                throw CancellationException(("Expected object to be tree, but actually ${obj.__typename}"))
            }

            val entries: List<TreeEntryItem> = obj.entries?.map { it.fragments.treeEntryItem } ?: emptyList()
            showEntries(entries)
        }
    }

    private fun loadRefs(forceRefresh: Boolean) {
        launch {
            val refs = gdd.getRefs(repo, getBranches = true, getTags = true).await(forceRefresh = forceRefresh)
            val entries = refs.entries(currentRef).map { it.vh() }
            val adapter = FastBindingAdapter()
            materialDialog {
                customListAdapter(adapter)
                adapter.add(entries)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        inflateMenu(R.menu.menu_repo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_branch -> loadRefs(forceRefresh = false)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
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
        private const val SAVED_STATE = "repo_saved_state"

        fun launch(context: Context, repo: GitNameAndOwner) {
            context.startActivity<RepoActivity>(intentBuilder = {
                putExtra(Args.repo, repo)
            })
        }
    }
}