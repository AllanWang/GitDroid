package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import ca.allanwang.gitdroid.codeview.pattern.LexerCache
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.databinding.ViewBlobBinding
import ca.allanwang.kau.utils.startActivity
import github.fragment.ObjectItem
import kotlinx.coroutines.launch

class BlobActivity : ToolbarActivity() {

    private lateinit var binding: ViewBlobBinding

    private val repo by repoExtra()
    private val fileName by stringExtra { name }
    private val oid by oidExtra()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = bindContent(R.layout.view_blob)
        supportActionBar?.also {
            it.title = fileName
        }

        launch {

            val blob: ObjectItem.AsBlob? = gdd.getRepoObject(repo, oid).await() as? ObjectItem.AsBlob
            val content = blob?.text ?: "Error"
            binding.codeview.setData(
                content,
                lexerCache.getLexer(
                    content,
                    fileName.substringAfterLast(".", "")
                )
            )
        }
    }

    companion object {
        val lexerCache: LexerCache = LexerCache(CodeLanguage.all())

        fun launch(context: Context, repo: GitNameAndOwner, fileName: String, oid: GitObjectID) {
            context.startActivity<BlobActivity>(intentBuilder = {
                putExtra(Args.repo, repo)
                putExtra(Args.name, fileName)
                putExtra(Args.oid, oid)
            })
        }
    }
}