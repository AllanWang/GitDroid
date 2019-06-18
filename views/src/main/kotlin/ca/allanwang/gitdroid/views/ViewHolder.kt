package ca.allanwang.gitdroid.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.databinding.ViewIssueItemBinding
import github.fragment.ShortIssueRowItem

typealias VHBindingType = ViewHolderBinding<*>

abstract class ViewHolderBinding<T : ViewDataBinding>(
    open val data: Any?,
    private val layoutRes: Int,
    val typeId: Int = layoutRes
) {

    abstract val dataId: Int?

    open fun T.create() {}

    abstract fun T.bind(position: Int, payloads: MutableList<Any>)

    open fun T.onRecycled() {}

    fun onCreate(parent: ViewGroup): View {
        val binding: T = DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutRes, parent, false)
        binding.create()
        return binding.root
    }

    fun onBind(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val binding: T = DataBindingUtil.getBinding(holder.itemView) ?: return
        binding.bind(position, payloads)
    }

    fun onRecycled(holder: RecyclerView.ViewHolder) {
        val binding: T = DataBindingUtil.getBinding(holder.itemView) ?: return
        binding.onRecycled()
        binding.unbind()
    }

    open fun isItemSame(vh: VHBindingType): Boolean = typeId == vh.typeId && dataId != null && dataId == vh.dataId
    open fun isContentSame(vh: VHBindingType): Boolean = typeId == vh.typeId && data == vh.data
    open fun changePayload(vh: VHBindingType): Any? = vh.data

}

class IssueVhBinding(override val data: ShortIssueRowItem) :
    ViewHolderBinding<ViewIssueItemBinding>(data, R.layout.view_issue_item) {

    override val dataId: Int?
        get() = data.databaseId

    override fun ViewIssueItemBinding.bind(position: Int, payloads: MutableList<Any>) {
        issue = data
    }
}