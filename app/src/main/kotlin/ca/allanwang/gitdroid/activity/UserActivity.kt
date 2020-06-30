package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.databinding.ViewRefreshRecyclerBinding
import ca.allanwang.gitdroid.item.clickHook
import ca.allanwang.gitdroid.utils.RvAnimation
import ca.allanwang.gitdroid.views.item.RepoVhBinding
import ca.allanwang.gitdroid.views.item.SlimEntryVhBinding
import ca.allanwang.gitdroid.views.item.vhFull
import ca.allanwang.kau.adapters.SingleFastAdapter
import ca.allanwang.kau.utils.launchMain
import ca.allanwang.kau.utils.startActivity
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.withSceneTransitionAnimation

class UserActivity : ToolbarActivity() {

    private lateinit var binding: ViewRefreshRecyclerBinding

    private val login by stringExtra { login }

    private val fastAdapter = SingleFastAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewRefreshRecyclerBinding.inflate(layoutInflater, contentRoot, true)
        supportActionBar?.also {
            it.title = login
        }
        fastAdapter.also {
            it.addEventHook(SlimEntryVhBinding.clickHook())
            it.addEventHook(RepoVhBinding.clickHook())
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
            val vh = user?.vhFull(this@UserActivity)!! // TODO handle missing user
            fastAdapter.add(vh)
        }
    }

    companion object {
        fun launch(context: Context, login: String, view: View? = null) {
            context.startActivity<UserActivity>(intentBuilder = {
                putExtra(Args.login, login)
            }, bundleBuilder = {
                if (view != null) {
                    withSceneTransitionAnimation(context, mapOf(view to context.string(R.string.transition_recycler)))
                }
            })
        }
    }
}