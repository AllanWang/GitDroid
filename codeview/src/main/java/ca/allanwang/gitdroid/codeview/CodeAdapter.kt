package ca.allanwang.gitdroid.codeview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.codeview.databinding.ViewItemCodeBinding
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.log10

class CodeAdapter : RecyclerView.Adapter<CodeViewHolder>() {

    private var data: List<CodeLine> = emptyList()
    private var maxLineNum: Double = 0.0


    suspend fun setData(content: String) {
        withContext(Dispatchers.Default) {
            val lines = content.split('\n')
            coroutineScope {
                val spans = lines.map { async { Highlighter.highlight(it) } }.awaitAll()
                val data = spans.mapIndexed { index, spannedString -> CodeLine(index + 1, spannedString) }
                withContext(Dispatchers.Main) {
                    this@CodeAdapter.data = data
                    maxLineNum = data.size.toDouble()
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeViewHolder {
        val binding: ViewItemCodeBinding =
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.view_item_code, parent, false)
        val holder = CodeViewHolder(binding.root)
        binding.codeItemLine.setEms(log10(maxLineNum).toInt() + 1)
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
        binding.codeItemLineNum.text = item.lineNumber?.toString()
        binding.codeItemLine.text = item.code
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