package ca.allanwang.gitdroid.utils

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

fun View.setCoordinatorLayoutScrollingBehaviour() {
    val params = layoutParams as? CoordinatorLayout.LayoutParams ?: return
    params.behavior = AppBarLayout.ScrollingViewBehavior(context, null)
}