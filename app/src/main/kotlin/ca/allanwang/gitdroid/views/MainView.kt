package ca.allanwang.gitdroid.views

import android.view.ViewGroup
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.BaseActivity
import ca.allanwang.gitdroid.databinding.ViewMainBinding

fun BaseActivity.bindMainView(parent: ViewGroup) {
    bindView<ViewMainBinding>(parent, R.layout.view_main) {
        mainBottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_bottom_feed -> {

                }
            }
            true
        }
    }

}