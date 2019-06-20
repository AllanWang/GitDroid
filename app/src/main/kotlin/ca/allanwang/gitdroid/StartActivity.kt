package ca.allanwang.gitdroid

import android.os.Bundle
import ca.allanwang.gitdroid.activity.BaseActivity
import ca.allanwang.gitdroid.activity.LoginActivity
import ca.allanwang.gitdroid.activity.RepoActivity
import ca.allanwang.kau.utils.startActivity

class StartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            prefs.token.isBlank() -> startActivity<LoginActivity>()
            else -> startActivity<RepoActivity>()
//            else -> startActivity<MainActivity>()
        }
    }
}