package ca.allanwang.gitdroid.activity

import android.content.Context
import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.base.LoadingActivity
import ca.allanwang.gitdroid.codeview.language.KotlinLang
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.databinding.ActivityBlobBinding
import ca.allanwang.kau.utils.startActivity
import github.fragment.ObjectItem
import kotlinx.coroutines.launch

class BlobActivity : LoadingActivity<ActivityBlobBinding>() {

    override val layoutRes: Int
        get() = R.layout.activity_blob

    val query by stringExtra(ARG_QUERY)
    private val _oidString by stringExtra(ARG_OID)
    val oid: GitObjectID
        get() = GitObjectID(_oidString)

    override fun onCreate2(savedInstanceState: Bundle?) {
        launch {
            val blob: ObjectItem.AsBlob? = gdd.getFileInfo(query, oid).await() as? ObjectItem.AsBlob
            val content = blob?.text ?: "Error"
            binding.blobCodeview.setData(content, KotlinLang)
        }
    }


    companion object {
        private const val ARG_QUERY = "arg_blob_query"
        private const val ARG_OID = "arg_blob_oid"

        fun launch(context: Context, query: String, oid: GitObjectID) {
            context.startActivity<BlobActivity>(intentBuilder = {
                putExtra(ARG_QUERY, query)
                putExtra(ARG_OID, oid.oid)
            })
        }
    }
}