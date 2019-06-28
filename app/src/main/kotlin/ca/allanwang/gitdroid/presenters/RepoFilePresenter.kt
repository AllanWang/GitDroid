package ca.allanwang.gitdroid.presenters

import android.os.Bundle
import android.os.Parcelable
import ca.allanwang.gitdroid.activity.BlobActivity
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.GitRef
import ca.allanwang.gitdroid.data.helpers.GitComparators
import ca.allanwang.gitdroid.views.components.PathCrumbsView
import ca.allanwang.gitdroid.views.databinding.ViewRepoFilesBinding
import ca.allanwang.gitdroid.views.item.GenericBindingItem
import ca.allanwang.gitdroid.views.item.TreeEntryVhBinding
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.gitdroid.views.utils.FastBindingAdapter
import ca.allanwang.gitdroid.views.utils.PathCrumb
import ca.allanwang.gitdroid.views.utils.lazyUi
import github.fragment.ObjectItem
import github.fragment.TreeEntryItem
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RepoFilePresenter(
    override val binding: ViewRepoFilesBinding,
    presenterContext: PresenterContext,
    val repo: GitNameAndOwner
) : BasePresenter(presenterContext) {

    private var currentRef: GitRef? = null

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

    init {
        binding.apply {
            repoRecycler.adapter = fastAdapter

            pathCrumbs.callback = { data ->
                loadFolder(data?.oid, false)
            }
            repoRefresh.setOnRefreshListener {
                loadFolder(pathCrumbs.getCurrentCrumb()?.oid, true)
            }
        }
        loadFolder(null)
    }

    @Parcelize
    private data class InstanceState(val crumbs: List<PathCrumb>) : Parcelable

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        if (pathCrumbs.getCrumbs().isNotEmpty()) {
            return
        }
        if (!restoreFolder(savedInstanceState)) {
            loadFolder(null)
        }
    }

    private fun restoreFolder(savedInstanceState: Bundle): Boolean {
        val state: InstanceState = savedInstanceState.getParcelable(tag) ?: return false
        val crumbs = state.crumbs
        if (crumbs.isEmpty()) {
            return false
        }
        pathCrumbs.setCrumbs(state.crumbs)
        loadFolder(state.crumbs.last().oid)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val crumbs = pathCrumbs.getCrumbs()
        val state = InstanceState(crumbs)
        outState.putParcelable(tag, state)
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

    fun loadTextBlob(name: String, oid: GitObjectID, forceRefresh: Boolean = false) {
        BlobActivity.launch(context, repo, name, oid)
    }

    fun loadFolder(oid: GitObjectID?, forceRefresh: Boolean = false) {
        binding.repoRefresh.isRefreshing = false
        fastAdapter.clear()
        launch {
            val obj = gdd.getRepoObject(repo, oid)
                .await(forceRefresh = forceRefresh)
            val entries: List<TreeEntryItem>? =
                when (obj) {
                    is ObjectItem.AsTree -> obj.entries?.map { it.fragments.treeEntryItem }
                    is ObjectItem.AsCommit -> obj.tree.entries?.map { it.fragments.treeEntryItem }
                    else -> throw CancellationException(("Expected object to be tree, but actually ${obj.__typename}"))
                }
            showEntries(entries ?: emptyList())
        }
    }

    override fun onBackPressed(): Boolean = pathCrumbs.onBackPressed()
}