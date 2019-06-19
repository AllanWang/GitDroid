package ca.allanwang.gitdroid.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.views.databinding.ViewUserBinding
import ca.allanwang.kau.utils.launchMain

class UserActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ViewUserBinding = DataBindingUtil.setContentView(this, R.layout.view_user)
        launchMain {
            val user = gdd.getProfile(me().login).await()
            binding.user = user?.user
        }
    }
}