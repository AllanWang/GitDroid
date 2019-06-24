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

    val query by stringExtra(ARG_QUERY)
    private val fileName by stringExtra(ARG_FILENAME)
    private val _oidString by stringExtra(ARG_OID)
    val oid: GitObjectID
        get() = GitObjectID(_oidString)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.also {
            it.title = fileName
        }

        launch {
            val blob: ObjectItem.AsBlob? = gdd.getFileInfo(query, oid).await() as? ObjectItem.AsBlob
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
        private const val ARG_QUERY = "arg_blob_query"
        private const val ARG_OID = "arg_blob_oid"
        private const val ARG_FILENAME = "arg_blob_file_name"

        val lexerCache: LexerCache = LexerCache(CodeLanguage.all())

        fun launch(context: Context, query: String, fileName: String, oid: GitObjectID) {
            context.startActivity<BlobActivity>(intentBuilder = {
                putExtra(ARG_QUERY, query)
                putExtra(ARG_FILENAME, fileName)
                putExtra(ARG_OID, oid.oid)
            })
        }
    }
}