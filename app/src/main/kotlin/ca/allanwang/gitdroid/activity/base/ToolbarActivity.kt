package ca.allanwang.gitdroid.activity.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.databinding.ActivityBaseToolbarBinding

abstract class ToolbarActivity : IntentActivity() {

    lateinit var toolbarBinding: ActivityBaseToolbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarBinding = bindContentView(R.layout.activity_base_toolbar)
        setSupportActionBar(toolbarBinding.viewToolbar.toolbar)
        supportActionBar?.also {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    fun <T : ViewDataBinding> bindContent(@LayoutRes layoutRes: Int): T =
        bindView(toolbarBinding.contentContainer, layoutRes, true)

}