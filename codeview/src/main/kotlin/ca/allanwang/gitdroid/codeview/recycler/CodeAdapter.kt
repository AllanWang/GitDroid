package ca.allanwang.gitdroid.codeview.recycler

import android.content.Context
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnNextLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.codeview.CodeViewLoader
import ca.allanwang.gitdroid.codeview.R
import ca.allanwang.gitdroid.codeview.databinding.ViewItemCodeBinding
import ca.allanwang.gitdroid.codeview.highlighter.CodeHighlighter
import ca.allanwang.gitdroid.codeview.highlighter.CodeTheme
import ca.allanwang.gitdroid.codeview.highlighter.splitCharSequence
import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import ca.allanwang.gitdroid.codeview.pattern.Lexer
import ca.allanwang.gitdroid.codeview.utils.CodeViewUtils
import ca.allanwang.gitdroid.codeview.utils.ceilInt
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.kau.utils.dimen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.log10

class CodeAdapter(context: Context) : RecyclerView.Adapter<CodeViewHolder>(),
    CodeViewLoader {

    private var data: List<CodeLine> = emptyList()
    // TODO add default theme
    internal var theme: CodeTheme? = null

    @Volatile
    private var prevLang: CodeLanguage? = null
    @Volatile
    private var prevLexer: Lexer? = null

    private var lineNumWidth: Int = 0
    private var lineCodeWidth: Int = 0
    private val lineMargins: Int = (context.dimen(R.dimen.code_line_horizontal_margins) * 2f).ceilInt()

    private lateinit var textPaint: TextPaint
    private lateinit var layoutManager: CodeLayoutManager

    fun bind(lineNumTextPaint: TextPaint, codeLinearLayout: CodeLayoutManager) {
        this.textPaint = lineNumTextPaint
        this.layoutManager = codeLinearLayout
    }

    private fun lineNumWidth(count: Float): Int {
        val digitCount = log10(count).toInt() + 1
        L.d { "Digit count $digitCount" }
        return CodeViewUtils.computeWidth(textPaint, "0".repeat(digitCount)).ceilInt()
    }

    override fun setCodeTheme(theme: CodeTheme) {
        if (this.theme === theme) {
            return
        }
        this.theme = theme
        notifyDataSetChanged()
    }

    override suspend fun setData(content: String, lang: CodeLanguage, theme: CodeTheme?) {
        withContext(Dispatchers.Main) {
            this@CodeAdapter.theme = theme
            val oldSize = data.size
            data = emptyList()
            notifyItemRangeRemoved(0, oldSize)
        }
        withContext(Dispatchers.Default) {
            L._d { "Received data ${content.length}" }
            var lexer = prevLexer
            val prevLang = prevLang
            if (lexer == null || prevLang == null || prevLang::class == lang::class) {
                lexer = Lexer(lang)
                prevLexer = lexer
            }
            L._d { "Create lexer" }
            val decorations = lexer.decorate(content)
            L._d { "Create decorations" }
            // TODO make default theme based on attributes
            val spannable = CodeHighlighter.highlight(content, decorations, theme ?: CodeTheme.default())
            L._d { "Highlight" }
            val lines = spannable.splitCharSequence('\n')
            L._d { "Split lines" }
            val maxWidth = CodeViewUtils.computeMaxWidth(textPaint, lines)
            L._d { "Max width $maxWidth ${layoutManager.width} $lineMargins" }
            withContext(Dispatchers.Main) {
                lineNumWidth = lineNumWidth(lines.size.toFloat()) + lineMargins
                lineCodeWidth = maxWidth + lineMargins
                layoutManager.setContentWidth(lineNumWidth + lineCodeWidth)
                data = lines.mapIndexed { i, s -> CodeLine(i, s) }
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeViewHolder {
        val binding: ViewItemCodeBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.view_item_code, parent, false
            )
        val holder = CodeViewHolder(binding.root)
        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition.takeIf { p -> p != RecyclerView.NO_POSITION } ?: return@setOnClickListener
            val data = it.getTag(R.id.code_view_item_data) as? CodeLine
                ?: return@setOnClickListener
            onClick(it, data, pos)
        }
        return holder
    }

    private fun onClick(view: View, data: CodeLine, position: Int) {

    }

    override fun onBindViewHolder(holder: CodeViewHolder, position: Int) {
        onBindViewHolder(holder, position, Collections.emptyList())
    }

    override fun onBindViewHolder(holder: CodeViewHolder, position: Int, payloads: MutableList<Any>) {
        val item: CodeLine = data.getOrNull(position) ?: return
        val binding: ViewItemCodeBinding = DataBindingUtil.getBinding(holder.itemView) ?: return

        // Unfortunately width modification doesn't work during onCreateViewHolder
        // Requesting a layout then causes the width to reset, as it isn't bound
        with(binding) {
            codeItemLineNum.text = item.lineNumber?.toString()
            codeItemLineNum.width = lineNumWidth
            codeItemLine.text = item.code
            codeItemLine.width = lineCodeWidth
            theme?.also {
                codeItemLineNum.setTextColor(it.lineNumTextColor)
                codeItemLineNum.setBackgroundColor(it.lineNumBg)
                root.setBackgroundColor(it.contentBg)
            }
            if (position == 0) {
                codeItemLine.doOnNextLayout {
                    L.d { "Measure ${codeItemLine.width} ${codeItemLine.measuredWidth}" }
                }
            }
        }
        holder.itemView.setTag(R.id.code_view_item_data, item)
    }

    override fun onViewRecycled(holder: CodeViewHolder) {
        super.onViewRecycled(holder)
        holder.itemView.setTag(R.id.code_view_item_data, null)
        val binding: ViewItemCodeBinding = DataBindingUtil.getBinding(holder.itemView) ?: return
        binding.codeItemLineNum.text = null
        binding.codeItemLine.text = null
        binding.unbind()
    }

    override fun getItemCount(): Int = data.size
}

data class CodeLine(val lineNumber: Int?, val code: CharSequence)


class CodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)