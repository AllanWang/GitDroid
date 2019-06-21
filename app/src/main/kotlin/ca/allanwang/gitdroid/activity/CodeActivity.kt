package ca.allanwang.gitdroid.activity

import android.os.Bundle
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.codeview.CodeAdapter
import ca.allanwang.gitdroid.codeview.databinding.ViewCodeFrameBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CodeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = bindContentView<ViewCodeFrameBinding>(R.layout.view_code_frame)
        val adapter = CodeAdapter()
        binding.codeViewRecycler.adapter = adapter
        launch {
            withContext(Dispatchers.Default) {
                val content = buildString {
                    for (i in (0..2000)) {
                        append("hello $i")
                        if (i != 0 && i % 20 == 0) {
                            append("\n")
                        }
                    }
                }
                adapter.setData(content)
            }
        }
    }
}