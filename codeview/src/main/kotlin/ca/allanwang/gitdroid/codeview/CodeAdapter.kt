package ca.allanwang.gitdroid.codeview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.codeview.databinding.ViewItemCodeBinding
import ca.allanwang.gitdroid.codeview.highlighter.CodeTheme
import ca.allanwang.gitdroid.codeview.language.CodeLanguage
import ca.allanwang.gitdroid.codeview.pattern.Lexer
import ca.allanwang.gitdroid.codeview.pattern.LexerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.log10

class CodeAdapter : RecyclerView.Adapter<CodeViewHolder>(), CodeViewLoader {

    private var data: List<CodeLine> = emptyList()
    private var ems: Int = 0
    // TODO add default theme
    internal var theme: CodeTheme? = null

    @Volatile
    private var prevLang: CodeLanguage? = null
    @Volatile
    private var prevOptions: LexerOptions? = null
    @Volatile
    private var prevLexer: Lexer? = null

    private fun emsDec(count: Float): Int =
        log10(count).toInt() + 1

    override fun setCodeTheme(theme: CodeTheme) {
        if (this.theme === theme) {
            return
        }
        this.theme = theme
        notifyDataSetChanged()
    }

    override suspend fun setData(content: String, lang: CodeLanguage, options: LexerOptions?, theme: CodeTheme?) {
        withContext(Dispatchers.Main) {
            this@CodeAdapter.theme = theme
            val oldSize = data.size
            data = emptyList()
            notifyItemRangeRemoved(0, oldSize)
        }
        withContext(Dispatchers.Default) {
            var lexer = prevLexer
            if (lexer == null || prevLang?.id != lang.id || prevOptions != options) {
                lexer = Lexer(lang, options)
                prevLexer = lexer
            }
            val lines = content.split('\n')
            withContext(Dispatchers.Main) {
                data = lines.mapIndexed { i, s -> CodeLine(i, s) }
                notifyDataSetChanged()
            }
//            coroutineScope {
//                val spans =
//                    lines.map { async { CodeHighlighter.highlight(it, Lexer(LexerOptions(KotlinLang))) } }.awaitAll()
//                val data = spans.mapIndexed { index, spannedString -> CodeLine(index + 1, spannedString) }
//                withContext(Dispatchers.Main) {
//                    this@CodeAdapter.data = data
//                    ems = emsDec(data.size.toFloat())
//                    notifyDataSetChanged()
//                }
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeViewHolder {
        val binding: ViewItemCodeBinding =
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.view_item_code, parent, false)
        val holder = CodeViewHolder(binding.root)
        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition.takeIf { p -> p != RecyclerView.NO_POSITION } ?: return@setOnClickListener
            val data = it.getTag(R.id.code_view_item_data) as? CodeLine ?: return@setOnClickListener
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

        with(binding) {
            codeItemLineNum.text = item.lineNumber?.toString()
            // Unfortunately this doesn't work during onCreateViewHolder
            // Requesting a layout then causes the width to reset, as it isn't bound
            codeItemLineNum.setEms(ems)
            codeItemLine.text = item.code
            theme?.also {
                codeItemLineNum.setTextColor(it.lineNumTextColor)
                codeItemLineNum.setBackgroundColor(it.lineNumBg)
                root.setBackgroundColor(it.contentBg)
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