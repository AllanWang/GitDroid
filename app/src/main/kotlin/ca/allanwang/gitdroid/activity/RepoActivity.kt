package ca.allanwang.gitdroid.activity

import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.views.databinding.ViewRepoFilesBinding

class RepoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = bindContentView<ViewRepoFilesBinding>(R.layout.view_repo_files)
    }
}