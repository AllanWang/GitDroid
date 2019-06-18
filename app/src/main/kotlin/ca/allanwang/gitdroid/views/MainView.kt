package ca.allanwang.gitdroid.views

import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.BaseActivity
import ca.allanwang.gitdroid.data.GitDroidData
import ca.allanwang.gitdroid.databinding.ViewMainBinding
import ca.allanwang.gitdroid.ktx.utils.L
import ca.allanwang.kau.animators.FadeScaleAnimatorAdd
import ca.allanwang.kau.animators.FadeScaleAnimatorRemove
import ca.allanwang.kau.animators.KauAnimator
import ca.allanwang.kau.animators.SlideAnimatorAdd
import ca.allanwang.kau.utils.KAU_BOTTOM
import ca.allanwang.kau.utils.launchMain
import ca.allanwang.kau.utils.snackbar
import com.apollographql.apollo.api.Response
import github.sql.GitUser
import kotlinx.coroutines.CancellationException

fun BaseActivity.bindMainView(parent: ViewGroup) {

    bindView<ViewMainBinding>(parent, R.layout.view_main) {

        val loaders = loaders().map { it.id to it }.toMap()

        val cache = mutableMapOf<Int, List<VHBindingType>>()

        val pending = mutableSetOf<Int>()

        // TODO set default
        var currentId: Int = R.id.nav_bottom_issues
        mainBottomNavigation.selectedItemId = currentId

        val adapter = Adapter.bind(mainRecycler)

        val samePanelAnimator = KauAnimator(
            addAnimator = SlideAnimatorAdd(KAU_BOTTOM, slideFactor = 2f),
            removeAnimator = FadeScaleAnimatorRemove()
        ).apply {
            addDuration = 500L
            interpolator = FastOutSlowInInterpolator()
        }
        val switchPanelAnimator =
            KauAnimator(addAnimator = FadeScaleAnimatorAdd(), removeAnimator = FadeScaleAnimatorRemove())

        mainRecycler.setHasFixedSize(false)

        /**
         * Submit a launch request on the main thread
         * Note that handling pending actions is dependent
         * on this execution occurring on one thread only
         */
        fun request(id: Int, forceRefresh: Boolean) {
            val loader = loaders[id]
            if (loader == null) {
                L.e { "Missing loader for main view" }
                return
            }
            val tag = loader::class.java.simpleName
            val samePanel = currentId == id
            val newItemAnimator = if (samePanel) samePanelAnimator else switchPanelAnimator
            // Setting animator cancels some animations, which we don't necessarily need
            if (mainRecycler.itemAnimator !== newItemAnimator) {
                mainRecycler.itemAnimator = newItemAnimator
            }
            currentId = id
            if (samePanel) {
                if (!forceRefresh || id in pending) {
                    return
                }
            } else {
                if (id in pending) {
                    adapter.data = emptyList()
                    return
                }
                val prev = cache[id]
                if (prev != null && !forceRefresh) {
                    adapter.data = prev
                    return
                }
            }
            pending.add(id)
            L._d { "Launch new load for $tag" }
            launchMain {
                mainRefresh.isRefreshing = true
                adapter.data = emptyList()
                with(loader) {
                    val data = loadData()
                    cache[id] = data
                    if (currentId == id) {
                        adapter.data = data
                    }
                }
            }.invokeOnCompletion {
                if (it is CancellationException || this@bindMainView.isDestroyed) {
                    return@invokeOnCompletion
                }
                if (it != null) {
                    snackbar(R.string.error_occurred)
                }
                pending.remove(id)
                if (pending.isEmpty()) {
                    mainRefresh.isRefreshing = false
                } else {
                    L._d { "Main pending ${pending.size} items" }
                }
            }
        }

        mainRefresh.setOnRefreshListener {
            request(currentId, true)
        }

        mainBottomNavigation.setOnNavigationItemSelectedListener {
            request(it.itemId, false)
            true
        }

        request(currentId, true)
    }

}

private fun loaders(): List<MainPanelLoader> = listOf(
    mainPanelLoader(R.id.nav_bottom_issues, { IssueVhBinding(it) }) {
        getIssues(it.login, count = 5)
    },
    mainPanelLoader(R.id.nav_bottom_prs, { PullRequestVhBinding(it) }) {
        getPullRequests(it.login, count = 5)
    },
    mainPanelLoader(R.id.nav_bottom_repos, { RepoVhBinding(it) }) {
        getRepos(it.login, count = 5)
    })


interface MainPanelLoader {

    val id: Int

    suspend fun BaseActivity.loadData(): List<VHBindingType>

}

fun <T> mainPanelLoader(
    id: Int,
    vhBinding: (T) -> VHBindingType,
    loader: suspend GitDroidData.(me: GitUser) -> Response<List<T>>
): MainPanelLoader {
    return object : MainPanelLoader {
        override val id: Int = id

        override suspend fun BaseActivity.loadData(): List<VHBindingType> {
            val me = me() ?: return emptyList()
            val result: List<T> = gdd.loader(me).await() ?: return emptyList()
            return result.map { vhBinding(it) }
        }
    }
}