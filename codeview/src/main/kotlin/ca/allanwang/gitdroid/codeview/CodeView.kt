package ca.allanwang.gitdroid.codeview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import ca.allanwang.gitdroid.codeview.databinding.ViewCodeFrameBinding
import ca.allanwang.gitdroid.codeview.highlighter.CodeTheme
import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import ca.allanwang.gitdroid.codeview.pattern.LexerOptions

class CodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val adapter: CodeAdapter = CodeAdapter()
) : FrameLayout(context, attrs, defStyleAttr), CodeViewLoader by adapter {

    private val binding: ViewCodeFrameBinding

    init {
        val inflater = context.getSystemService<LayoutInflater>() ?: throw RuntimeException("No layout inflater")
        binding = DataBindingUtil.inflate(inflater, R.layout.view_code_frame, this, true)
        binding.codeViewRecycler.adapter = adapter
    }

}

interface CodeViewLoader {
    suspend fun setData(content: String, lang: CodeLanguage, options: LexerOptions? = null, theme: CodeTheme? = null)
    fun setCodeTheme(theme: CodeTheme)
}