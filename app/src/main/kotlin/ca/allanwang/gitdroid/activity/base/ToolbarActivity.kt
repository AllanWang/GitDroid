package ca.allanwang.gitdroid.activity.base

import android.os.Bundle
import android.view.ViewGroup
import ca.allanwang.gitdroid.databinding.ActivityBaseToolbarBinding

abstract class ToolbarActivity : IntentActivity() {

    lateinit var toolbarBinding: ActivityBaseToolbarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbarBinding = ActivityBaseToolbarBinding.inflate(layoutInflater)
        setContentView(toolbarBinding.root)
        setSupportActionBar(toolbarBinding.viewToolbar.toolbar)
        supportActionBar?.also {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    protected val contentRoot: ViewGroup get() = toolbarBinding.contentContainer

}