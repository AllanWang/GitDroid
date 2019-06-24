package ca.allanwang.gitdroid.activity.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.databinding.ActivityBaseToolbarBinding

abstract class ToolbarActivity<Binding : ViewDataBinding> : IntentActivity() {

    lateinit var toolbarBinding: ActivityBaseToolbarBinding
    lateinit var binding: Binding

    abstract val layoutRes: Int
        @LayoutRes get


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