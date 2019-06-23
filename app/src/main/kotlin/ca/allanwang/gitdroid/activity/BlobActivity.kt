package ca.allanwang.gitdroid.activity

import android.os.Bundle
import ca.allanwang.gitdroid.codeview.CodeView
import ca.allanwang.gitdroid.codeview.highlighter.CodeTheme
import ca.allanwang.gitdroid.codeview.language.KotlinLang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlobActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val codeView = CodeView(this)
        setContentView(codeView)
        launch {
            withContext(Dispatchers.Default) {
                val content = """
                    package source

                    /**
                     * KtDocs
                     */
                    class Test {

                        // Single comment
                        data class A(val s: Int, val y: Boolean) data class A(val s: Int, val y: Boolean) data class A(val s: Int, val y: Boolean) data class A(val s: Int, val y: Boolean) data class A(val s: Int, val y: Boolean) data class A(val s: Int, val y: Boolean) data class A(val s: Int, val y: Boolean)

                        init {
                            val lit = 0
                            val lit2 = 0xff00ff
                            val s = "hello"
                            val s2 =
                                ""${'"'}
                                long
                                string
                                ""${'"'}.trimIndent()
                        }

                        /*
                         * Multi line comment
                         */
                        @Volatile
                        var hello: Int = 2

                        fun a(t: Test) {
                            with(t) {
                                return@with
                            }
                        }


                    }


                """.trimIndent().repeat(20)
                codeView.setData(content, KotlinLang)
            }
        }
    }
}