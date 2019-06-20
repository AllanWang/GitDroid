package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.views.Adapter
import ca.allanwang.gitdroid.views.databinding.ViewRepoFilesBinding
import ca.allanwang.kau.utils.startActivity
import github.fragment.FullRepo
import kotlinx.coroutines.launch

class RepoActivity : LoadingActivity<ViewRepoFilesBinding>() {

    override val layoutRes: Int = R.layout.view_repo_files

    val query by stringExtra(ARG_QUERY)

    lateinit var treeAdapter: Adapter

    override fun onCreate2(savedInstanceState: Bundle?) {
        treeAdapter = Adapter.bind(binding.repoRecycler)
        launch {
            val repo = gdd.getRepo(query).await()
            val defaultBranch = repo.defaultBranchRef
            if (defaultBranch == null) {

            } else {
                val entries = defaultBranch
                    .target
                    .let { it as FullRepo.AsCommit }
                    .tree
                    .entries
                    ?.map { it.fragments.treeEntryItem }?: emptyList()
            }

        }
    }

    companion object {
        private const val ARG_QUERY = "arg_query"

        fun launch(context: Context, nameWithOwner: String) {
            context.startActivity<RepoActivity>(intentBuilder = {
                putExtra(ARG_QUERY, nameWithOwner)
            })
        }
    }
}