package ca.allanwang.gitdroid.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.databinding.ActivityUserBinding
import ca.allanwang.gitdroid.views.Adapter
import ca.allanwang.gitdroid.views.vhFull
import ca.allanwang.kau.utils.launchMain

class UserActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityUserBinding = DataBindingUtil.setContentView(this, R.layout.activity_user)
        val adapter = Adapter.bind(binding.recycler)
        launchMain {
            val user = gdd.getProfile(me().login).await()?.user ?: return@launchMain
            adapter.data = user.vhFull(this@UserActivity)
        }
    }
}