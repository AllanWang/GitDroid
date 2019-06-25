package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.ToolbarActivity
import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import ca.allanwang.gitdroid.codeview.pattern.LexerCache
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.databinding.ViewBlobBinding
import ca.allanwang.kau.utils.startActivity
import github.fragment.ObjectItem
import kotlinx.coroutines.launch

class BlobActivity : ToolbarActivity<ViewBlobBinding>() {

    override val layoutRes: Int
        get() = R.layout.view_blob

    private val login by stringExtra { login }
    private val repo by stringExtra { repo }
    private val fileName by stringExtra { fileName }
    private val oid by parcelableExtra<GitObjectID> { oid }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.also {
            it.title = fileName
        }

        launch {

            val blob: ObjectItem.AsBlob? = gdd.getObject(login, repo, oid).await() as? ObjectItem.AsBlob
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

        fun launch(context: Context, login: String, repo: String, fileName: String, oid: GitObjectID) {
            context.startActivity<BlobActivity>(intentBuilder = {
                putExtra(Args.login, login)
                putExtra(Args.repo, repo)
                putExtra(Args.fileName, fileName)
                putExtra(Args.oid, oid)
            })
        }
    }
}