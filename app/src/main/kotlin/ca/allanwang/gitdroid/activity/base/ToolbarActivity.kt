package ca.allanwang.gitdroid.activity.base

import android.os.Bundle
import android.view.MenuItem
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}