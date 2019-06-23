package ca.allanwang.gitdroid.activity

import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.BaseActivity
import ca.allanwang.gitdroid.databinding.ActivityUserBinding
import ca.allanwang.gitdroid.views.Adapter
import ca.allanwang.gitdroid.views.vhFull
import ca.allanwang.kau.utils.launchMain

class UserActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = bindContentView<ActivityUserBinding>(R.layout.activity_user)
        val adapter = Adapter.bind(binding.recycler)
        launchMain {
            val user = gdd.getProfile(me().login).await()
            adapter.data = user.vhFull(this@UserActivity)
        }
    }
}