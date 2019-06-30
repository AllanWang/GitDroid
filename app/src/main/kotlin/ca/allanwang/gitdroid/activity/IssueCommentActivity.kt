package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.databinding.HeaderIssueCommentBinding
import ca.allanwang.gitdroid.databinding.ViewRefreshRecyclerBinding
import ca.allanwang.gitdroid.item.clickHook
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.gitdroid.utils.RvAnimation
import ca.allanwang.gitdroid.views.utils.FastBindingAdapter
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.views.item.IssueCommentVhBinding
import ca.allanwang.gitdroid.views.item.PlaceholderVhBinding
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.kau.utils.startActivity
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.withSceneTransitionAnimation
import kotlinx.coroutines.launch

class IssueCommentActivity : ToolbarActivity<ViewRefreshRecyclerBinding>() {


    private val repo by repoExtra()

    private val issueNumber by intExtra { issueNumber }

    private val issueName by stringExtra { name }

    override val layoutRes: Int
        get() = R.layout.view_refresh_recycler

    private val fastAdapter: FastBindingAdapter =
        FastBindingAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.recycler.also {
            it.adapter = fastAdapter
        }
        fastAdapter.also {
            it.addEventHook(IssueCommentVhBinding.clickHook())
        }
        supportActionBar?.also {
            it.title = getString(R.string.issue_n, issueNumber)
            it.subtitle = repo.nameWithOwner
        }
        val headerBinding: HeaderIssueCommentBinding = bindView(appbar, R.layout.header_issue_comment, false)
        addAppBarView(headerBinding.root)
        headerBinding.title.text = issueName
        binding.refresh.setOnRefreshListener {
            fastAdapter.clear()
            loadIssue()
        }
        loadIssue()
    }

    private fun loadIssue() {
        L.d { "Load issue" }
        binding.refresh.isRefreshing = true
        launch {
            val issue = gdd.getIssue(repo, issueNumber).await()
            val vhs = issue?.comments?.nodes?.map { it.fragments.shortIssueComment.vh() } ?: emptyList()
            binding.refresh.isRefreshing = false
            if (vhs.isNotEmpty()) {
                RvAnimation.set(binding.recycler, fastAdapter)
                fastAdapter.add(vhs)
            } else {
                RvAnimation.FAST.set(binding.recycler)
                fastAdapter.add(PlaceholderVhBinding(R.string.no_comments))
            }
        }
    }

    companion object {

        fun launch(context: Context, repo: GitNameAndOwner, name: String, issueNumber: Int, view: View? = null) {
            context.startActivity<IssueCommentActivity>(intentBuilder = {
                putExtra(Args.repo, repo)
                putExtra(Args.name, name)
                putExtra(Args.issueNumber, issueNumber)
            }, bundleBuilder = {
                if (view != null) {
                    withSceneTransitionAnimation(context, mapOf(view to context.string(R.string.transition_recycler)))
                }
            })
        }
    }
}