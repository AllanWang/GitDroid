package ca.allanwang.gitdroid.views

import android.view.ViewGroup
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.BaseActivity
import ca.allanwang.gitdroid.databinding.ViewMainBinding
import github.fragment.ShortIssueRowItem
import kotlinx.coroutines.launch

fun BaseActivity.bindMainView(parent: ViewGroup) {
    bindView<ViewMainBinding>(parent, R.layout.view_main) {
        val adapter = Adapter.bind(mainRecycler)

        suspend fun loadIssues() {
            val me = me() ?: return
            val issues: List<ShortIssueRowItem> =
                gdd.getIssues(me.login).await() ?: return
            adapter.data = issues.map { IssueVhBinding(it) }
        }

        mainBottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_bottom_feed -> {
                }
                R.id.nav_bottom_issues -> launch { loadIssues() }
            }
            true
        }
    }

}