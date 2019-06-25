package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.databinding.ViewRefreshRecyclerBinding
import ca.allanwang.gitdroid.utils.RvAnimation
import ca.allanwang.gitdroid.views.FastBindingAdapter
import ca.allanwang.gitdroid.views.item.vhFull
import ca.allanwang.kau.utils.launchMain
import ca.allanwang.kau.utils.startActivity

class UserActivity : ToolbarActivity<ViewRefreshRecyclerBinding>() {

    override val layoutRes: Int
        get() = R.layout.view_refresh_recycler

    private val login by stringExtra { login }

    private val fastAdapter = FastBindingAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.also {
            it.title = login
        }
        binding.recycler.also {
            it.adapter = fastAdapter
            RvAnimation.FAST.set(it)
        }
        binding.refresh.setOnRefreshListener {
            loadUser()
        }
        loadUser()
    }

    private fun loadUser() {
        binding.refresh.isRefreshing = true
        fastAdapter.clear()
        launchMain {
            val user = gdd.getProfile(login).await()
            binding.refresh.isRefreshing = false
            fastAdapter.add(user.vhFull(this@UserActivity))
        }
    }

    companion object {
        fun launch(context: Context, login: String) {
            context.startActivity<UserActivity>(intentBuilder = {
                putExtra(Args.login, login)
            })
        }
    }
}