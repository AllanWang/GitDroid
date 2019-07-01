package ca.allanwang.gitdroid.utils

import android.view.LayoutInflater
import android.view.View
import androidx.annotation.MenuRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.GitCallVhList
import ca.allanwang.gitdroid.activity.base.BaseActivity
import ca.allanwang.gitdroid.databinding.ActivityBaseToolbarBinding
import ca.allanwang.gitdroid.databinding.ViewBottomNavBinding
import ca.allanwang.gitdroid.databinding.ViewBottomNavRecyclerBinding
import ca.allanwang.gitdroid.databinding.ViewToolbarBinding
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.gitdroid.views.item.GenericBindingItem
import ca.allanwang.gitdroid.views.utils.FastBindingAdapter
import ca.allanwang.kau.utils.launchMain
import ca.allanwang.kau.utils.snackbar
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CancellationException

fun View.setCoordinatorLayoutScrollingBehaviour() {
    val params = layoutParams as? CoordinatorLayout.LayoutParams ?: return
    params.behavior = AppBarLayout.ScrollingViewBehavior(context, null)
}

fun ViewToolbarBinding.addAppBarView(v: View) {
    (toolbar.layoutParams as? AppBarLayout.LayoutParams)?.scrollFlags =
        AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
    appbar.addView(v)
}

fun ActivityBaseToolbarBinding.addBottomNavBar(): BottomNavigationView {
    val inflater = LayoutInflater.from(coordinator.context)
    val binding = ViewBottomNavBinding.inflate(inflater, coordinator, true)
    return binding.bottomNavigation
}


fun <T : ViewDataBinding> ViewToolbarBinding.addAppBarView(layoutRes: Int): T {
    val binding: T = DataBindingUtil.inflate(LayoutInflater.from(appbar.context), layoutRes, appbar, false)
    addAppBarView(binding.root)
    return binding
}

val BottomNavigationView.firstId: Int
    get() = menu.getItem(0).itemId

fun BottomNavigationView.verifyLoaders(ids: Set<Int>) {
    val navIds = menu.children.mapTo(HashSet()) { it.itemId }
    if (navIds.isEmpty()) {
        throw IllegalArgumentException("Verifying bottom nav view when no items are attached")
    }
    if (navIds != ids) {
        throw IllegalArgumentException("Bottom nav loader mismatch; expected $navIds, got $ids")
    }
}

interface ViewBottomNavRecyclerConfig {
    val menuRes: Int
        @MenuRes get

    val loaders: Map<Int, suspend () -> GitCallVhList>

    val activity: BaseActivity

    val adapter: FastBindingAdapter
}

interface ViewBottomNavRecyclerLoader {
    /**
     * Submit a launch request on the main thread
     * Note that handling pending actions is dependent
     * on this execution occurring on one thread only
     */
    fun request(id: Int, forceRefresh: Boolean)
}

fun ViewBottomNavRecyclerBinding.setLoader(config: ViewBottomNavRecyclerConfig): ViewBottomNavRecyclerLoader {
    bottomNavigation.menu.clear()
    bottomNavigation.inflateMenu(config.menuRes)
    val loaders = config.loaders
    val fastAdapter = config.adapter

    bottomNavigation.verifyLoaders(loaders.keys)

    val cache = mutableMapOf<Int, List<GenericBindingItem>>()

    val pending = mutableSetOf<Int>()

    var currentId: Int = bottomNavigation.firstId

    recycler.adapter = fastAdapter

    fun request(id: Int, forceRefresh: Boolean) {
        val loader = loaders.getValue(id)
        val samePanel = currentId == id
        currentId = id
        if (samePanel) {
            if (!forceRefresh || id in pending) {
                return
            }
        } else {
            if (id in pending) {
                fastAdapter.clear()
                return
            }
            val prev = cache[id]
            if (prev != null && !forceRefresh) {
                RvAnimation.INSTANT.set(recycler)
                fastAdapter.clear()
                fastAdapter.add(prev)
                refresh.isRefreshing = false
                return
            }
        }
        pending.add(id)
        RvAnimation.FAST.set(recycler)
        refresh.isRefreshing = true
        fastAdapter.clear()
        with(config.activity) {
            lifecycleScope.launchMain {
                val data = loader().await(forceRefresh = samePanel || forceRefresh)
                cache[id] = data
                if (currentId == id) {
                    refresh.isRefreshing = false
                    RvAnimation.set(recycler, fastAdapter)
                    fastAdapter.add(data)
                }
            }.invokeOnCompletion {
                if (it is CancellationException || isDestroyed) {
                    return@invokeOnCompletion
                }
                if (it != null) {
                    snackbar(R.string.error_occurred)
                }
                pending.remove(id)
                if (pending.isEmpty()) {
                    refresh.isRefreshing = false
                } else {
                    L._d { "Bottom nav bar pending ${pending.size} items" }
                }
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

    return object : ViewBottomNavRecyclerLoader {
        override fun request(id: Int, forceRefresh: Boolean) {
            request(id, forceRefresh)
        }
    }
}