package ca.allanwang.gitdroid

import android.os.Bundle
import ca.allanwang.gitdroid.activity.IssueCommentActivity
import ca.allanwang.gitdroid.activity.LoginActivity
import ca.allanwang.gitdroid.activity.MainActivity
import ca.allanwang.gitdroid.activity.RepoActivity
import ca.allanwang.gitdroid.activity.base.BaseActivity
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.gitNameAndOwner
import ca.allanwang.gitdroid.data.gitRef
import ca.allanwang.kau.utils.startActivity
import kotlinx.coroutines.launch

class StartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            prefs.token.isBlank() -> startActivity<LoginActivity>()
//            else -> startActivity<BlobActivity>()
//            else -> startActivity<MainActivity>()
            !BuildConfig.DEBUG -> startActivity<MainActivity>()
            else -> launch {
                repoActivityTest()
            }
        }
    }

    private fun issueCommentTest() = IssueCommentActivity.launch(
        this,
        GitNameAndOwner("KEEP", "Kotlin"), "Sample", 155
    )

    private suspend fun repoActivityTest() {
        RepoActivity.launch(this@StartActivity, GitNameAndOwner("KAU", "AllanWang"), null)
    }
}