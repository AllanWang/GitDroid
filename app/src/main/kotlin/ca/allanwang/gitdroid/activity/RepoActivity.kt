package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.GitRef
import ca.allanwang.gitdroid.data.gitRef
import ca.allanwang.gitdroid.fragment.RepoFileFragment
import ca.allanwang.gitdroid.fragment.RepoOverviewFragment
import ca.allanwang.gitdroid.fragment.base.BaseFragment
import ca.allanwang.gitdroid.utils.addBottomNavBar
import ca.allanwang.gitdroid.utils.firstId
import ca.allanwang.gitdroid.utils.verifyLoaders
import ca.allanwang.gitdroid.viewmodel.RepoViewModel
import ca.allanwang.gitdroid.views.item.RefEntryVhBinding
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.gitdroid.views.utils.entries
import ca.allanwang.gitdroid.views.utils.lazyUi
import ca.allanwang.kau.adapters.SingleFastAdapter
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.startActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.list.customListAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class RepoActivity : ToolbarActivity() {

    lateinit var model: RepoViewModel

    lateinit var bottomNavBar: BottomNavigationView

    private var currentRef: GitRef? = null

    private val refAdapter: SingleFastAdapter by lazyUi {
        SingleFastAdapter().apply {
            onClickListener = { _, _, item, _ ->
                if (item is RefEntryVhBinding) {
                    if (!item.data.current) {
                        model.ref.value = item.data.ref.gitRef()
                    }
                    refDialog?.dismiss()
                    true
                } else {
                    false
                }
            }
        }
    }

    private var refDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = viewModel()
        bottomNavBar = toolbarBinding.addBottomNavBar()
        bottomNavBar.inflateMenu(R.menu.repo_bottom_nav)
        model.repo.observe(this) {
            supportActionBar?.title = it.nameWithOwner
        }
        model.ref.observe(this) {
            supportActionBar?.subtitle = it?.name
        }
        val loaders = mapOf(
            R.id.nav_bottom_overview to RepoOverviewFragment::class,
            R.id.nav_bottom_code to RepoFileFragment::class,
            R.id.nav_bottom_commits to RepoFileFragment::class
        ).mapValues { (_, v) -> lazyUi { v.java.newInstance() } }

        bottomNavBar.verifyLoaders(loaders.keys)

        bottomNavBar.setOnNavigationItemSelectedListener {
            showFragment(loaders.getValue(it.itemId).value)
            true
        }

        showFragment(loaders.getValue(bottomNavBar.firstId).value)
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(toolbarBinding.contentContainer.id, fragment).commit()
    }

    private fun currentFragment(): BaseFragment<*>? =
        supportFragmentManager.findFragmentById(toolbarBinding.contentContainer.id) as? BaseFragment<*>

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        inflateMenu(R.menu.menu_repo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_branch -> loadRefs(forceRefresh = false)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun loadRefs(forceRefresh: Boolean) {
        launch {
            val refs =
                gdd.getRefs(model.repo.value, getBranches = true, getTags = true).await(forceRefresh = forceRefresh)
            val entries = refs?.entries(currentRef?.oid)?.map { it.vh() } ?: emptyList()
            refAdapter.setNewList(entries)
            refDialog = materialDialog {
                customListAdapter(refAdapter)
                onCancel {
                    refDialog = null
                }
                onDismiss {
                    refDialog = null
                }
            }
        }
    }

    override fun finish() {
        BlobActivity.lexerCache.clear()
        super.finish()
    }

    override fun onBackPressed() {
        if (currentFragment()?.onBackPressed() == true) {
            return
        }
        super.onBackPressed()
    }

    companion object {
        fun launch(context: Context, repo: GitNameAndOwner, ref: GitRef?) {
            context.startActivity<RepoActivity>(intentBuilder = {
                putExtra(Args.repo, repo)
                putExtra(Args.ref, ref)
            })
        }
    }
}