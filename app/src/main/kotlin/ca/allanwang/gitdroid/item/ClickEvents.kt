package ca.allanwang.gitdroid.item

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ca.allanwang.gitdroid.activity.RepoActivity
import ca.allanwang.gitdroid.views.databinding.ViewRepoBinding
import ca.allanwang.gitdroid.views.databinding.ViewSlimEntryBinding
import ca.allanwang.gitdroid.views.item.BindingClickEventHook
import ca.allanwang.gitdroid.views.item.RepoVhBinding
import ca.allanwang.gitdroid.views.item.SlimEntryVhBinding
import com.mikepenz.fastadapter.FastAdapter

fun SlimEntryVhBinding.Companion.clickHook() =
    object : BindingClickEventHook<ViewSlimEntryBinding, SlimEntryVhBinding>(SlimEntryVhBinding) {

        override fun ViewSlimEntryBinding.onBind(viewHolder: RecyclerView.ViewHolder): View? = root

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<SlimEntryVhBinding>,
            item: SlimEntryVhBinding
        ) {
            item.data.onClick?.invoke(v)
        }

    }

fun RepoVhBinding.Companion.clickHook() =
    object : BindingClickEventHook<ViewRepoBinding, RepoVhBinding>(RepoVhBinding) {

        override fun ViewRepoBinding.onBind(viewHolder: RecyclerView.ViewHolder): View? = root

        override fun onClick(v: View, position: Int, fastAdapter: FastAdapter<RepoVhBinding>, item: RepoVhBinding) {
            RepoActivity.launch(v.context, item.data.owner.login, item.data.name)
        }
    }
