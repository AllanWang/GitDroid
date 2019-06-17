package ca.allanwang.gitdroid.views

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.views.databinding.ViewIssueItemBinding
import github.fragment.ShortIssueRowItem

abstract class ViewHolderBinding<T : ViewDataBinding>(private val layoutRes: Int, val typeId: Int = layoutRes) {

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

}

class IssueVhBinding(val issue: ShortIssueRowItem) : ViewHolderBinding<ViewIssueItemBinding>(R.layout.view_issue_item) {

    override fun ViewIssueItemBinding.bind(position: Int, payloads: MutableList<Any>) {
        setVariable(BR.issue, issue)
    }

}