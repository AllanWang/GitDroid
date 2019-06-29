package ca.allanwang.gitdroid.fragment.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.utils.RvAnimation
import ca.allanwang.gitdroid.viewmodel.base.*
import ca.allanwang.gitdroid.views.item.PlaceholderVhBinding
import ca.allanwang.gitdroid.views.utils.fastAdapter

abstract class BaseFragment<Binding : ViewDataBinding> : Fragment() {

    val binding: Binding?
        get() = view?.let { DataBindingUtil.getBinding(it) }

    abstract val layoutRes: Int
        @LayoutRes get

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: Binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        return binding.root
    }

    @MainThread
    fun <T> LiveData<T>.observe(handler: (T) -> Unit) =
        observe(viewLifecycleOwner, handler)

    @MainThread
    fun <T> LoadingLiveData<T>.observeLoadingData(
        refresh: SwipeRefreshLayout,
        recycler: RecyclerView,
        onLoading: ((refresh: SwipeRefreshLayout, recycler: RecyclerView) -> Unit)? = null,
        onFailed: ((refresh: SwipeRefreshLayout, recycler: RecyclerView) -> Unit)? = null,
        onLoad: (refresh: SwipeRefreshLayout, recycler: RecyclerView, data: T) -> Unit
    ): Observer<LoadingData<T>> {
        return observe(viewLifecycleOwner) { result ->
            when (result) {
                is Loading -> {
                    if (onLoading != null) {
                        onLoading(refresh, recycler)
                    } else {
                        refresh.isRefreshing = true
                        recycler.fastAdapter.clear()
                    }
                }
                is FailedLoad -> {
                    if (onFailed != null) {
                        onFailed(refresh, recycler)
                    } else {
                        refresh.isRefreshing = false
                        recycler.fastAdapter.apply {
                            clear()
                            add(PlaceholderVhBinding(R.string.error))
                        }
                    }
                }
                is LoadedData -> {
                    refresh.isRefreshing = false
                    recycler.fastAdapter.clear()
                    onLoad(refresh, recycler, result.data)
                }
            }
        }
    }

    inline fun <reified T : ViewModel> viewModel(factory: ViewModelProvider.Factory? = BaseViewModel.Factory(arguments)) =
        ViewModelProviders.of(
            activity ?: throw RuntimeException("Activity not created, cannot initiate viewmodel"),
            factory
        ).get(T::class.java)

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DataBindingUtil.getBinding<Binding>(view)?.onViewCreated(view, savedInstanceState)
    }

    open fun Binding.onViewCreated(view: View, savedInstanceState: Bundle?) {}


    final override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding?.onActivityCreated(savedInstanceState)
    }

    open fun Binding.onActivityCreated(savedInstanceState: Bundle?) {}

    /**
     * Called when a back press event is received.
     * Return true if it is handled
     */
    open fun onBackPressed(): Boolean = false
}