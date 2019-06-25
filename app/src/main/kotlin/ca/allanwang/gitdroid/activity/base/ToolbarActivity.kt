package ca.allanwang.gitdroid.activity.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.databinding.ActivityBaseToolbarBinding
import com.google.android.material.appbar.AppBarLayout

abstract class ToolbarActivity<Binding : ViewDataBinding> : IntentActivity() {

    lateinit var toolbarBinding: ActivityBaseToolbarBinding

    val toolbar: Toolbar
        get() = toolbarBinding.viewToolbar.toolbar

    val appbar: AppBarLayout
        get() = toolbarBinding.viewToolbar.appbar

    lateinit var binding: Binding

    abstract val layoutRes: Int
        @LayoutRes get

    fun addAppBarView(v: View) {
        (toolbar.layoutParams as? AppBarLayout.LayoutParams)?.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                    AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS or
                    AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
        appbar.addView(v)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarBinding = bindContentView(R.layout.activity_base_toolbar)
        setSupportActionBar(toolbarBinding.viewToolbar.toolbar)
        supportActionBar?.also {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        binding = bindView(toolbarBinding.contentContainer, layoutRes, true)
    }

}