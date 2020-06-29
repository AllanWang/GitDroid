package ca.allanwang.gitdroid.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.databinding.ViewRefreshRecyclerBinding
import ca.allanwang.gitdroid.fragment.base.BaseFragment
import ca.allanwang.gitdroid.item.clickHook
import ca.allanwang.gitdroid.viewmodel.RepoViewModel
import ca.allanwang.gitdroid.views.item.RepoVhBinding
import ca.allanwang.gitdroid.views.item.SlimEntryVhBinding
import ca.allanwang.gitdroid.views.item.vhFull
import ca.allanwang.gitdroid.views.itemdecoration.BottomNavDecoration
import ca.allanwang.gitdroid.views.utils.lazyUi
import ca.allanwang.kau.adapters.SingleFastAdapter

class RepoOverviewFragment : BaseFragment<ViewRefreshRecyclerBinding>() {

    private val fastAdapter: SingleFastAdapter by lazyUi {
        SingleFastAdapter().apply {
            addEventHook(RepoVhBinding.clickHook())
            addEventHook(SlimEntryVhBinding.clickHook())
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
    ): ViewRefreshRecyclerBinding = ViewRefreshRecyclerBinding.inflate(inflater, container, false).also { it.init() }

    private fun ViewRefreshRecyclerBinding.init() {
        recycler.apply {
            adapter = fastAdapter
            addItemDecoration(BottomNavDecoration(context))
        }
        refresh.setOnRefreshListener {
            model.fullRepoLoader().execute(true)
        }
        model.fullRepo.observeRefresh(refresh)
        model.fullRepo.observeAdapter(fastAdapter) { fullRepo ->
            if (fullRepo != null) {
                fastAdapter.add(fullRepo.vhFull(recycler.context))
            }
        }
    }

}