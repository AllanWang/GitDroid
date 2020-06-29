package ca.allanwang.gitdroid.codeview.recycler

import android.content.Context
import android.text.TextPaint
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.codeview.CodeViewData
import ca.allanwang.gitdroid.codeview.R
import ca.allanwang.gitdroid.codeview.highlighter.CodeTheme
import ca.allanwang.gitdroid.codeview.utils.CodeViewUtils
import ca.allanwang.gitdroid.codeview.utils.ceilInt
import ca.allanwang.kau.utils.dimen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.log10

class CodeAdapter(context: Context) : RecyclerView.Adapter<CodeViewHolder>() {

    private var data: List<CodeLine> = emptyList()

    private var lineNumWidth: Int = 0
    private var lineCodeWidth: Int = 0
    private val lineMargins: Int =
        (context.dimen(R.dimen.code_line_horizontal_margins) * 2f).ceilInt()

    internal lateinit var theme: CodeTheme
    private lateinit var textPaint: TextPaint
    private lateinit var layoutManager: CodeLayoutManager

    fun bind(lineNumTextPaint: TextPaint, codeLinearLayout: CodeLayoutManager, theme: CodeTheme) {
        this.theme = theme
        this.textPaint = lineNumTextPaint
        this.layoutManager = codeLinearLayout
    }

    private fun lineNumWidth(count: Float): Int {
        val digitCount = when {
            count <= 0 -> return 0
            count < 10 -> return 1
            else -> log10(count).toInt() + 1
        }
        return CodeViewUtils.computeWidth(textPaint, "0".repeat(digitCount)).ceilInt()
    }

    fun clear() {
        val oldSize = data.size
        if (oldSize == 0) {
            return
        }
        data = emptyList()
        notifyItemRangeRemoved(0, oldSize)
    }

    /**
     * Compute new bounds and set provided code lines in adapter.
     * Note that this is computationally expensive and should not be executed on the main thread
     */
    suspend fun setData(data: CodeViewData, theme: CodeTheme) {
        val maxWidth = CodeViewUtils.computeMaxWidth(textPaint, data.lines.map { it.code })
        val lineNumWidth = lineNumWidth(data.lineCount.toFloat())
        withContext(Dispatchers.Main) {
            this@CodeAdapter.also {
                it.theme = theme
                it.lineNumWidth = lineNumWidth + lineMargins
                it.lineCodeWidth = maxWidth + lineMargins
                it.layoutManager.setContentWidth(it.lineNumWidth + it.lineCodeWidth)
                val previouslyEmpty = it.data.isEmpty()
                it.data = data.lines
                if (previouslyEmpty) {
                    notifyItemRangeInserted(0, data.lines.size)
                } else {
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeViewHolder {
//        val binding: ViewItemCodeBinding =
//            DataBindingUtil.inflate(
//                LayoutInflater.from(parent.context),
//                R.layout.view_item_code, parent, false
//            )
//        val holder = CodeViewHolder(binding.root)
//        holder.itemView.setOnClickListener {
//            val pos = holder.adapterPosition.takeIf { p -> p != RecyclerView.NO_POSITION } ?: return@setOnClickListener
//            val data = it.getTag(R.id.code_view_item_data) as? CodeLine
//                ?: return@setOnClickListener
//            onClick(it, data, pos)
//        }
//        return holder
        TODO()
    }

    private fun onClick(view: View, data: CodeLine, position: Int) {

    }

    override fun onBindViewHolder(holder: CodeViewHolder, position: Int) {
        onBindViewHolder(holder, position, Collections.emptyList())
    }

    override fun onBindViewHolder(
        holder: CodeViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val item: CodeLine = data.getOrNull(position) ?: return
//        val binding: ViewItemCodeBinding = DataBindingUtil.getBinding(holder.itemView) ?: return
//
//        // Unfortunately width modification doesn't work during onCreateViewHolder
//        // Requesting a layout then causes the width to reset, as it isn't bound
//        with(binding) {
//            codeItemLineNum.text = item.lineNumber?.toString()
//            codeItemLineNum.width = lineNumWidth
//            codeItemLine.text = item.code
//            codeItemLine.width = lineCodeWidth
//            theme.also {
//                codeItemLineNum.setTextColor(it.lineNumTextColor)
//                codeItemLineNum.setBackgroundColor(it.lineNumBg)
//                root.setBackgroundColor(it.contentBg)
//            }
//        }
        holder.itemView.setTag(R.id.code_view_item_data, item)
    }

    override fun onViewRecycled(holder: CodeViewHolder) {
        super.onViewRecycled(holder)
        holder.itemView.setTag(R.id.code_view_item_data, null)
//        val binding: ViewItemCodeBinding = DataBindingUtil.getBinding(holder.itemView) ?: return
//        binding.codeItemLineNum.text = null
//        binding.codeItemLine.text = null
//        binding.unbind()
    }

    override fun getItemCount(): Int = data.size
}

data class CodeLine(val lineNumber: Int?, val code: CharSequence)


class CodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)