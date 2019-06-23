package ca.allanwang.gitdroid.codeview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import ca.allanwang.gitdroid.codeview.databinding.ViewCodeFrameBinding
import ca.allanwang.gitdroid.codeview.databinding.ViewItemCodeBinding
import ca.allanwang.gitdroid.codeview.highlighter.*
import ca.allanwang.gitdroid.codeview.pattern.Lexer
import ca.allanwang.gitdroid.codeview.recycler.CodeAdapter
import ca.allanwang.gitdroid.codeview.recycler.CodeItemDecorator
import ca.allanwang.gitdroid.codeview.recycler.CodeLayoutManager
import ca.allanwang.gitdroid.codeview.recycler.CodeLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


class CodeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val codeAdapter: CodeAdapter

    private val binding: ViewCodeFrameBinding

    private val defaultTheme: CodeTheme

    init {
        val inflater = context.getSystemService<LayoutInflater>() ?: throw RuntimeException("No layout inflater")

        val scrap: ViewItemCodeBinding = DataBindingUtil.inflate(inflater, R.layout.view_item_code, this, false)
        val textPaint = scrap.codeItemLine.paint
        scrap.unbind()
        val codeLayoutManager = CodeLayoutManager(context).apply {
            initialPrefetchItemCount = 10
        }

        defaultTheme = CodeAttrThemeBuilder.default().build(context, attrs) { CodeTheme.default() }

        codeAdapter = CodeAdapter(context)
        codeAdapter.bind(textPaint, codeLayoutManager, defaultTheme)

        binding = DataBindingUtil.inflate(inflater, R.layout.view_code_frame, this, true)
        binding.codeViewRecycler.apply {
            layoutManager = codeLayoutManager
            adapter = codeAdapter
            addItemDecoration(CodeItemDecorator(context))
        }
    }

    /**
     * Update code theme and reload the adapter
     */
    fun setCodeTheme(theme: CodeTheme? = null) {
        val trueTheme = theme ?: defaultTheme
        if (codeAdapter.theme === trueTheme) {
            return
        }
        codeAdapter.theme = trueTheme
        codeAdapter.notifyDataSetChanged()
    }

    /**
     * Clear adapter, compute code lines, and add lines to the adapter.
     * Note that this is computationally expensive and should not be executed on the main thread.
     * As this is the main entry point to updating the codeview, we enforce a non ui dispatcher by default
     */
    suspend fun setData(
        content: String,
        lexer: Lexer,
        theme: CodeTheme? = null,
        coroutineContext: CoroutineContext = Dispatchers.Default
    ) {
        withContext(Dispatchers.Main) {
            codeAdapter.clear()
        }
        withContext(coroutineContext) {
            val trueTheme = theme ?: defaultTheme
            val decorations = lexer.decorate(content)
            val result = CodeHighlighter.highlight(content, decorations, SpannableStringHighlightBuilder(trueTheme))
            val lines = result.splitCharSequence('\n').mapIndexed { i, line -> CodeLine(i + 1, line) }
            val data = CodeViewData(lines, lines.size)
            codeAdapter.setData(data, trueTheme)
        }
    }

}

data class CodeViewData(val lines: List<CodeLine>, val lineCount: Int)