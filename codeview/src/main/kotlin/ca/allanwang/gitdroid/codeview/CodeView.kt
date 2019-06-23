package ca.allanwang.gitdroid.codeview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import ca.allanwang.gitdroid.codeview.databinding.ViewCodeFrameBinding
import ca.allanwang.gitdroid.codeview.databinding.ViewItemCodeBinding
import ca.allanwang.gitdroid.codeview.highlighter.CodeTheme
import ca.allanwang.gitdroid.codeview.language.impl.CodeLanguage
import ca.allanwang.gitdroid.codeview.recycler.CodeAdapter
import ca.allanwang.gitdroid.codeview.recycler.CodeItemDecorator
import ca.allanwang.gitdroid.codeview.recycler.CodeLayoutManager


class CodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val codeAdapter: CodeAdapter = CodeAdapter(
        context
    )
) : FrameLayout(context, attrs, defStyleAttr), CodeViewLoader by codeAdapter {


    private val binding: ViewCodeFrameBinding

    init {
        val inflater = context.getSystemService<LayoutInflater>() ?: throw RuntimeException("No layout inflater")

        val scrap: ViewItemCodeBinding = DataBindingUtil.inflate(inflater, R.layout.view_item_code, this, false)
        val textPaint = scrap.codeItemLine.paint
        scrap.unbind()
        val codeLayoutManager = CodeLayoutManager(context).apply {
            initialPrefetchItemCount = 10
        }
        codeAdapter.bind(textPaint, codeLayoutManager)

        binding = DataBindingUtil.inflate(inflater, R.layout.view_code_frame, this, true)
        binding.codeViewRecycler.apply {
            layoutManager = codeLayoutManager
            adapter = codeAdapter
            addItemDecoration(CodeItemDecorator(context))
        }

    }

}

interface CodeViewLoader {
    suspend fun setData(content: String, lang: CodeLanguage, theme: CodeTheme? = null)
    fun setCodeTheme(theme: CodeTheme)
}