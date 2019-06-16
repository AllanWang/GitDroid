package ca.allanwang.gitdroid

import android.os.Bundle
import ca.allanwang.gitdroid.activity.LoginActivity
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.utils.startActivity

class StartActivity : KauBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity<LoginActivity>()
    }
}