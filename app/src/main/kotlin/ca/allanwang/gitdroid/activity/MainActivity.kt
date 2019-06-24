package ca.allanwang.gitdroid.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.BaseActivity
import ca.allanwang.gitdroid.data.GitCall
import ca.allanwang.gitdroid.data.lmap
import ca.allanwang.gitdroid.databinding.ActivityMainBinding
import ca.allanwang.gitdroid.item.clickHook
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.gitdroid.views.FastBindingAdapter
import ca.allanwang.gitdroid.views.item.RepoVhBinding
import ca.allanwang.gitdroid.views.item.GenericBindingItem
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.kau.animators.FadeScaleAnimatorAdd
import ca.allanwang.kau.animators.FadeScaleAnimatorRemove
import ca.allanwang.kau.animators.KauAnimator
import ca.allanwang.kau.animators.SlideAnimatorAdd
import ca.allanwang.kau.utils.KAU_BOTTOM
import ca.allanwang.kau.utils.launchMain
import ca.allanwang.kau.utils.snackbar
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CancellationException

typealias GitCallVhList = GitCall<List<GenericBindingItem>>

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var bindings: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = bindContentView(R.layout.activity_main)
        bindings.bind()
        bindings.bindContent()
    }

    private fun ActivityMainBinding.bind() {
        setSupportActionBar(viewToolbar.toolbar)
        val toggle = ActionBarDrawerToggle(
            this@MainActivity, drawerLayout, viewToolbar.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this@MainActivity)
    }

    private suspend fun loadRepos(): GitCallVhList = gdd.getUserRepos(me().login).lmap { it.vh() }
    private suspend fun loadIssues(): GitCallVhList = gdd.getIssues(me().login).lmap { it.vh() }
    private suspend fun loadPullRequests(): GitCallVhList = gdd.getPullRequests(me().login).lmap { it.vh() }

    private fun ActivityMainBinding.bindContent() {

        val loaders: Map<Int, suspend () -> GitCallVhList> = mapOf(
            R.id.nav_bottom_repos to ::loadRepos,
            R.id.nav_bottom_issues to ::loadIssues,
            R.id.nav_bottom_prs to ::loadPullRequests
        )

        val cache = mutableMapOf<Int, List<GenericBindingItem>>()

        var lastClearTime: Long = -1

        val changeThreshold = 100L

        val pending = mutableSetOf<Int>()

        var currentId: Int = bottomNavigation.menu.getItem(0).itemId

        val fastAdapter = FastBindingAdapter()

        fastAdapter.addEventHook(RepoVhBinding.clickHook())

        recycler.adapter = fastAdapter

        val fadeAnimator = KauAnimator(
            addAnimator = SlideAnimatorAdd(KAU_BOTTOM, slideFactor = 2f),
            removeAnimator = FadeScaleAnimatorRemove()
        ).apply {
            addDuration = 500L
            interpolator = FastOutSlowInInterpolator()
        }
        val fancyAnimator =
            KauAnimator(addAnimator = FadeScaleAnimatorAdd(), removeAnimator = FadeScaleAnimatorRemove())

        recycler.setHasFixedSize(false)

        /**
         * Submit a launch request on the main thread
         * Note that handling pending actions is dependent
         * on this execution occurring on one thread only
         */
        fun request(id: Int, forceRefresh: Boolean) {
            val loader = loaders[id]
            if (loader == null) {
                L.fail { "Missing loader for main view" }
                return
            }
            val tag = loader::class.java.simpleName
            val samePanel = currentId == id
            val newItemAnimator = when {
                samePanel -> fadeAnimator
                System.currentTimeMillis() - lastClearTime < changeThreshold -> fadeAnimator
                else -> fancyAnimator
            }
            // Setting animator cancels some animations, which we don't necessarily need
            if (recycler.itemAnimator !== newItemAnimator) {
                recycler.itemAnimator = newItemAnimator
            }
            currentId = id
            if (samePanel) {
                if (!forceRefresh || id in pending) {
                    return
                }
            } else {
                if (id in pending) {
                    fastAdapter.clear()
                    lastClearTime = System.currentTimeMillis()
                    return
                }
                val prev = cache[id]
                if (prev != null && !forceRefresh) {
                    fastAdapter.add(prev)
                    return
                }
            }
            pending.add(id)
            L._d { "Launch new load for $tag" }
            refresh.isRefreshing = true
            fastAdapter.clear()
            lastClearTime = System.currentTimeMillis()
            launchMain {
                val data = loader().await(forceRefresh = samePanel)
                cache[id] = data
                if (currentId == id) {
                    fastAdapter.add(data)
                }
            }.invokeOnCompletion {
                if (it is CancellationException || this@MainActivity.isDestroyed) {
                    return@invokeOnCompletion
                }
                if (it != null) {
                    snackbar(R.string.error_occurred)
                }
                pending.remove(id)
                if (pending.isEmpty()) {
                    refresh.isRefreshing = false
                } else {
                    L._d { "Main pending ${pending.size} items" }
                }
            }
        }

        refresh.setOnRefreshListener {
            request(currentId, true)
        }

        bottomNavigation.setOnNavigationItemSelectedListener {
            request(it.itemId, false)
            true
        }

        request(currentId, true)

    }

    override fun onBackPressed() {
        if (bindings.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            bindings.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle the camera action
            }
            else -> snackbar("Coming soon!")
        }
        bindings.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
