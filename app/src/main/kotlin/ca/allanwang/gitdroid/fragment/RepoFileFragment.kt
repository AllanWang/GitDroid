package ca.allanwang.gitdroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.BlobActivity
import ca.allanwang.gitdroid.fragment.base.BaseFragment
import ca.allanwang.gitdroid.utils.RvAnimation
import ca.allanwang.gitdroid.viewmodel.RepoViewModel
import ca.allanwang.gitdroid.views.databinding.ViewRepoFilesBinding
import ca.allanwang.gitdroid.views.item.PlaceholderVhBinding
import ca.allanwang.gitdroid.views.item.TreeEntryVhBinding
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.gitdroid.views.itemdecoration.BottomNavDecoration
import ca.allanwang.gitdroid.views.utils.PathCrumb
import ca.allanwang.gitdroid.views.utils.lazyUi
import ca.allanwang.kau.adapters.SingleFastAdapter
import github.fragment.TreeEntryItem

class RepoFileFragment : BaseFragment<ViewRepoFilesBinding>() {

    private val fastAdapter: SingleFastAdapter by lazyUi {
        SingleFastAdapter().apply {
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

    private lateinit var model: RepoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = viewModel()
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ViewRepoFilesBinding =
        ViewRepoFilesBinding.inflate(inflater, container, false).also { it.init() }

    private fun ViewRepoFilesBinding.init() {
        repoRecycler.apply {
            adapter = fastAdapter
            addItemDecoration(BottomNavDecoration(context))
        }

        RvAnimation.FAST.set(repoRecycler)

        // TODO position isn't currently in viewmodel
        repoPathCrumbs.callback = { oid ->
            model.entryLoader(oid).execute(false)
        }
        repoRefresh.setOnRefreshListener {
            model.entryLoader(repoPathCrumbs.getCurrentCrumb()?.oid).execute(true)
        }
        model.repo.observe {
            model.entryLoader(null).execute(false)
        }
        model.ref.observe {
            repoPathCrumbs.setCrumbs(it?.oid, emptyList())
            model.entryLoader(it?.oid).execute()
        }
        model.entries.apply {
            observeRefresh(repoRefresh, 1000L)
            observeAdapter(fastAdapter) { data ->
                val vhs = data.map { it.vh() }
                fastAdapter.apply {
                    if (vhs.isEmpty()) {
                        add(PlaceholderVhBinding(R.string.error)) // TODO
                    } else {
                        add(vhs)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fastAdapter.clear()
    }

//    @Parcelize
//    private data class InstanceState(val crumbs: List<PathCrumb>) : Parcelable


//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        if (pathCrumbs.getCrumbs().isNotEmpty()) {
//            return
//        }
//        if (!restoreFolder(savedInstanceState)) {
//            loadFolder(null)
//        }
//    }
//
//    private fun restoreFolder(savedInstanceState: Bundle): Boolean {
//        val state: InstanceState = savedInstanceState.getParcelable(tag) ?: return false
//        val crumbs = state.crumbs
//        if (crumbs.isEmpty()) {
//            return false
//        }
//        pathCrumbs.setCrumbs(state.crumbs)
//        loadFolder(state.crumbs.last().oid)
//        return true
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        val crumbs = pathCrumbs.getCrumbs()
//        val state = InstanceState(crumbs)
//        outState.putParcelable(tag, state)
//    }

    private fun onClick(data: TreeEntryItem) {
        val obj = data.obj
        if (obj is TreeEntryItem.AsBlob) {
            if (obj.isBinary) {
                // todo
            } else {
                context?.also {
                    BlobActivity.launch(it, model.repo.value, data.name, data.oid)
                }
            }
        } else {
            _binding?.repoPathCrumbs?.addCrumb(PathCrumb(data.name, data.oid))
            model.entryLoader(data.oid).execute()
        }
    }

    override fun onBackPressed(): Boolean = _binding?.repoPathCrumbs?.onBackPressed() ?: false
}
