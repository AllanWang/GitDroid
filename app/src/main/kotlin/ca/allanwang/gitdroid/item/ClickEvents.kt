package ca.allanwang.gitdroid.item

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.activity.RepoActivity
import ca.allanwang.gitdroid.views.item.RepoVhBinding
import ca.allanwang.gitdroid.views.item.SlimEntryVhBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook

fun SlimEntryVhBinding.Companion.clickHook(): ClickEventHook<SlimEntryVhBinding> =
    object : ClickEventHook<SlimEntryVhBinding>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? =
            (viewHolder as? SlimEntryVhBinding.ViewHolder)?.binding?.root

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<SlimEntryVhBinding>,
            item: SlimEntryVhBinding
        ) {
            item.data.onClick?.invoke(v)
        }

    }

fun RepoVhBinding.Companion.clickHook(): ClickEventHook<RepoVhBinding> =
    object : ClickEventHook<RepoVhBinding>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? =
            (viewHolder as? RepoVhBinding.ViewHolder)?.binding?.root

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<RepoVhBinding>, item: RepoVhBinding) {
            RepoActivity.launch(v.context, item.data.nameWithOwner)
        }
    }
