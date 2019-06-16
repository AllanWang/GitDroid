package ca.allanwang.gitdroid

import android.os.Bundle
import ca.allanwang.gitdroid.activity.LoginActivity
import ca.allanwang.gitdroid.activity.MainActivity
import ca.allanwang.gitdroid.utils.Prefs
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.utils.startActivity

class StartActivity : KauBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            Prefs.token.isBlank() -> startActivity<LoginActivity>()
            else -> startActivity<MainActivity>()
        }
    }
}