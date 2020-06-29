package ca.allanwang.gitdroid.fragment.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.viewbinding.ViewBinding
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.viewmodel.base.*
import ca.allanwang.gitdroid.views.components.SwipeRefreshLayout
import ca.allanwang.gitdroid.views.item.PlaceholderVhBinding
import ca.allanwang.kau.adapters.SingleFastAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseFragment<Binding : ViewBinding> : Fragment() {

    protected var _binding: Binding? = null

    val binding: Binding get() = _binding!!

    abstract fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = createBinding(inflater, container, savedInstanceState)
        return _binding?.root
    }

    @MainThread
    fun <T> LiveData<T>.observe(handler: (T) -> Unit) =
        observe(viewLifecycleOwner, handler)

    /**
     * Observe loading cycles for the refresh layout.
     * Refreshing can optionally be delayed if responses are typically quick, to avoid blinking the indicator.
     * Set to a value below 0L to disable.
     * Refresh cancellation always occurs, in case the user chooses to refresh manually
     */
    @MainThread
    fun <T> LoadingLiveData<T>.observeRefresh(
        refresh: SwipeRefreshLayout,
        delayMillis: Long = 0L
    ): Observer<LoadingData<T>> {
        var pending: Job? = null
        val scope = viewLifecycleOwner.lifecycleScope
        return observe(viewLifecycleOwner) { result ->
            when (result) {
                is Loading -> {
                    when {
                        delayMillis == 0L -> refresh.isRefreshing = true
                        delayMillis > 0L -> pending = scope.launch {
                            delay(delayMillis)
                            refresh.isRefreshing = true
                        }
                        // Else ignore
                    }
                }
                is FailedLoad, is LoadedData -> {
                    pending?.cancel()
                    pending = null
                    refresh.isRefreshing = false
                }
            }
        }
    }


    @MainThread
    fun <T> LoadingLiveData<T>.observeAdapter(
        adapter: SingleFastAdapter,
        onLoad: (data: T) -> Unit
    ): Observer<LoadingData<T>> {
        return observe(viewLifecycleOwner) { result ->
            adapter.clear()
            when (result) {
                is FailedLoad -> {
                    adapter.add(PlaceholderVhBinding(R.string.error))
                }
                is LoadedData -> {
                    onLoad(result.data)
                }
            }
        }
    }

    inline fun <reified T : ViewModel> viewModel(
        factory: ViewModelProvider.Factory? = BaseViewModel.Factory(
            arguments
        )
    ) = ViewModelProviders.of(
        activity ?: throw RuntimeException("Activity not created, cannot initiate viewmodel"),
        factory
    ).get(T::class.java)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Called when a back press event is received.
     * Return true if it is handled
     */
    open fun onBackPressed(): Boolean = false
}