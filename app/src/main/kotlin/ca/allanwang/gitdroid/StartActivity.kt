package ca.allanwang.gitdroid

import android.os.Bundle
import ca.allanwang.gitdroid.activity.IssueCommentActivity
import ca.allanwang.gitdroid.activity.LoginActivity
import ca.allanwang.gitdroid.activity.MainActivity
import ca.allanwang.gitdroid.activity.base.BaseActivity
import ca.allanwang.gitdroid.views.GitNameAndOwner
import ca.allanwang.kau.utils.startActivity

class StartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            prefs.token.isBlank() -> startActivity<LoginActivity>()
//            else -> RepoActivity.launch(this, "AllanWang/KAU")
//            else -> startActivity<BlobActivity>()
            else -> IssueCommentActivity.launch(this, GitNameAndOwner("KEEP", "Kotlin"), "Sample", 155)
//            else -> startActivity<MainActivity>()
        }
    }
}