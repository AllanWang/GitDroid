package ca.allanwang.gitdroid.fragment

import android.os.Bundle
import android.view.View
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

    override fun ViewRefreshRecyclerBinding.onViewCreated(view: View, savedInstanceState: Bundle?) {
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

    override val layoutRes: Int
        get() = R.layout.view_refresh_recycler

}