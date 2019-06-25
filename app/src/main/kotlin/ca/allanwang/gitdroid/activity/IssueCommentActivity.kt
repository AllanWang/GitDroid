package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.IntentActivity
import ca.allanwang.gitdroid.databinding.ActivityIssueCommentBinding
import ca.allanwang.gitdroid.utils.setCoordinatorLayoutScrollingBehaviour
import ca.allanwang.gitdroid.views.FastBindingAdapter
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.kau.utils.startActivity
import kotlinx.coroutines.launch

class IssueCommentActivity : IntentActivity() {

    private val login by stringExtra { login }

    private val repo by stringExtra { repo }

    private val issueNumber by intExtra { issueNumber }

    lateinit var binding: ActivityIssueCommentBinding

    private val fastAdapter: FastBindingAdapter = FastBindingAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindContentView(R.layout.activity_issue_comment)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.also {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        binding.contentContainer.setCoordinatorLayoutScrollingBehaviour()
        binding.vrr.recycler.also {
            it.adapter = fastAdapter
        }
        launch {
            val issue = gdd.getIssue(login, repo, issueNumber).await()
            val vhs = issue.comments.nodes?.map { it.fragments.shortIssueComment.vh() } ?: emptyList()
            fastAdapter.add(vhs)
        }
    }


    companion object {

        fun launch(context: Context, login: String, repo: String, issueNumber: Int) {
            context.startActivity<IssueCommentActivity>(intentBuilder = {
                putExtra(Args.login, login)
                putExtra(Args.repo, repo)
                putExtra(Args.issueNumber, issueNumber)
            })
        }
    }
}