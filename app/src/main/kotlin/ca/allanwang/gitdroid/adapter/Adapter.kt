package ca.allanwang.gitdroid.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import github.GetLabelsQuery
import java.util.*

class MainAdapter : RecyclerView.Adapter<MainViewHolder>() {

    var data: List<MainViewHolderData> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        TODO("not implemented")
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemCount(): Int {
        TODO("not implemented")
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        onBindViewHolder(holder, position, Collections.emptyList())
    }

    override fun onViewRecycled(holder: MainViewHolder) {
        holder.itemView.setTag()
        holder.adapterPosition
        super.onViewRecycled(holder)
    }
}

interface ViewHolderModel {
    val viewType: Int
}

sealed class MainViewHolderData : ViewHolderModel {
    companion object {
        const val REPO = 0
    }
}



class RepoVHModel(val data: GetLabelsQuery.Repository) {

}

sealed class MainViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)