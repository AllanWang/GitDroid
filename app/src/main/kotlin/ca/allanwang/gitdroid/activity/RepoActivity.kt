package ca.allanwang.gitdroid.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.GitRef
import ca.allanwang.gitdroid.data.gitRef
import ca.allanwang.gitdroid.databinding.ViewViewpagerBinding
import ca.allanwang.gitdroid.fragment.RepoFileFragment
import ca.allanwang.gitdroid.viewmodel.RepoViewModel
import ca.allanwang.gitdroid.views.item.RefEntryVhBinding
import ca.allanwang.gitdroid.views.item.vh
import ca.allanwang.gitdroid.views.utils.FastBindingAdapter
import ca.allanwang.gitdroid.views.utils.entries
import ca.allanwang.gitdroid.views.utils.lazyUi
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.startActivity
import com.afollestad.materialdialogs.list.customListAdapter
import kotlinx.coroutines.launch

class RepoActivity : ToolbarActivity<ViewViewpagerBinding>() {

    override val layoutRes: Int
        get() = R.layout.view_viewpager

    lateinit var model: RepoViewModel

    private var currentRef: GitRef? = null

    private val refAdapter: FastBindingAdapter by lazyUi {
        FastBindingAdapter().apply {
            onClickListener = { _, _, item, _ ->
                if (item is RefEntryVhBinding) {
                    currentRef = item.data.ref.gitRef()
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model = viewModel()
        binding.viewpager.adapter = RepoPagerAdapter(supportFragmentManager)
    }

    @SuppressLint("WrongConstant")
    class RepoPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment = items[position].java.newInstance()

        override fun getCount(): Int = items.size

        val items = arrayOf(RepoFileFragment::class)
    }

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
            materialDialog {
                customListAdapter(refAdapter)
            }
        }
    }

    override fun finish() {
        BlobActivity.lexerCache.clear()
        super.finish()
    }

    override fun onBackPressed() {
        // TODO
        super.onBackPressed()
    }

    companion object {
        fun launch(context: Context, repo: GitNameAndOwner) {
            context.startActivity<RepoActivity>(intentBuilder = {
                putExtra(Args.repo, repo)
            })
        }
    }
}