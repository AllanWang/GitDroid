package ca.allanwang.gitdroid.activity

import android.os.Bundle
import ca.allanwang.gitdroid.codeview.CodeView
import ca.allanwang.gitdroid.codeview.highlighter.CodeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CodeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val codeView = CodeView(this)
        setContentView(codeView)
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
//                codeView.setData(content)
//                codeView.setCodeTheme(CodeTheme.default())
            }
        }
    }
}