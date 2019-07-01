package ca.allanwang.gitdroid.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.BaseActivity
import ca.allanwang.gitdroid.data.gql.GitCall
import ca.allanwang.gitdroid.data.gql.lmap
import ca.allanwang.gitdroid.databinding.ActivityMainBinding
import ca.allanwang.gitdroid.item.clickHook
import ca.allanwang.gitdroid.utils.ViewBottomNavRecyclerConfig
import ca.allanwang.gitdroid.utils.setLoader
import ca.allanwang.gitdroid.views.item.GenericBindingItem
import ca.allanwang.gitdroid.views.item.IssuePrVhBinding
import ca.allanwang.gitdroid.views.item.RepoVhBinding
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.gitdroid.views.itemdecoration.BottomNavDecoration
import ca.allanwang.gitdroid.views.utils.FastBindingAdapter
import ca.allanwang.kau.utils.snackbar
import com.google.android.material.navigation.NavigationView

typealias GitCallVhList = GitCall<List<GenericBindingItem>>

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var bindings: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = bindContentView(R.layout.activity_main)
        bindings.bind()
        bindLoader()
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

    private suspend fun loadRepos(): GitCallVhList = gdd.searchUserRepos(me().login).lmap { it.vh() }
    private suspend fun loadIssues(): GitCallVhList = gdd.searchUserIssues(me().login).lmap { it.vh() }
    private suspend fun loadPullRequests(): GitCallVhList = gdd.searchUserPullRequests(me().login).lmap { it.vh() }

    @SuppressLint("PrivateResource")
    private fun bindLoader() {
        val config = object : ViewBottomNavRecyclerConfig {
            override val menuRes: Int
                get() = R.menu.main_bottom_nav
            override val loaders = mapOf(
                R.id.nav_bottom_repos to ::loadRepos,
                R.id.nav_bottom_issues to ::loadIssues,
                R.id.nav_bottom_prs to ::loadPullRequests
            )
            override val activity: BaseActivity = this@MainActivity
            override val adapter = FastBindingAdapter().apply {
                addEventHook(RepoVhBinding.clickHook())
                addEventHook(IssuePrVhBinding.clickHook())
            }

        }
        bindings.viewBottomNavRecycler.recycler.apply {
            recycledViewPool.setMaxRecycledViews(RepoVhBinding.layoutRes, 20)
            addItemDecoration(BottomNavDecoration(this@MainActivity))
        }
        bindings.viewBottomNavRecycler.setLoader(config)
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
