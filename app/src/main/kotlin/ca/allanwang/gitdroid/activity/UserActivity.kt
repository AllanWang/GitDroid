package ca.allanwang.gitdroid.activity

import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.databinding.ViewRefreshRecyclerBinding
import ca.allanwang.gitdroid.views.FastBindingAdapter
import ca.allanwang.gitdroid.views.item.vhFull
import ca.allanwang.kau.utils.launchMain

class UserActivity : ToolbarActivity<ViewRefreshRecyclerBinding>() {

    override val layoutRes: Int
        get() = R.layout.view_refresh_recycler

    private val fastAdapter = FastBindingAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.recycler.adapter = fastAdapter
        launchMain {
            val user = gdd.getProfile(me().login).await()
            fastAdapter.add(user.vhFull(this@UserActivity))
        }
    }
}