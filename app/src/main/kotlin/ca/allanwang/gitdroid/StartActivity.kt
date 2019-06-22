package ca.allanwang.gitdroid

import android.os.Bundle
import ca.allanwang.gitdroid.activity.BaseActivity
import ca.allanwang.gitdroid.activity.BlobActivity
import ca.allanwang.gitdroid.activity.LoginActivity
import ca.allanwang.kau.utils.startActivity

class StartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            prefs.token.isBlank() -> startActivity<LoginActivity>()
//            else -> RepoActivity.launch(this, "AllanWang/KAU")
            else -> startActivity<BlobActivity>()
//            else -> startActivity<MainActivity>()
        }
    }
}