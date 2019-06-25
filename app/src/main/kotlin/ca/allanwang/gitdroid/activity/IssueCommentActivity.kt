package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.IntentActivity
import ca.allanwang.gitdroid.databinding.ActivityIssueCommentBinding
import ca.allanwang.gitdroid.views.FastBindingAdapter
import ca.allanwang.kau.utils.startActivity
import kotlinx.coroutines.launch

class IssueCommentActivity : IntentActivity() {

    private val login by stringExtra { login }

    private val repo by stringExtra { repo }

    private val issueNumer by intExtra { issueNumber }

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
        launch {

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