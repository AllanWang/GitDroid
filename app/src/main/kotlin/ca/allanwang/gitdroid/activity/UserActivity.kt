package ca.allanwang.gitdroid.activity

import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.databinding.ViewUserBinding
import ca.allanwang.gitdroid.views.Adapter
import ca.allanwang.gitdroid.views.vhFull
import ca.allanwang.kau.utils.launchMain

class UserActivity : ToolbarActivity<ViewUserBinding>() {

    override val layoutRes: Int
        get() = R.layout.view_user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val adapter = Adapter.bind(binding.recycler)
        launchMain {
            val user = gdd.getProfile(me().login).await()
            adapter.data = user.vhFull(this@UserActivity)
        }
    }
}