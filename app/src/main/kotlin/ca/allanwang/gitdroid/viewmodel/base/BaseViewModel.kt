package ca.allanwang.gitdroid.viewmodel.base

import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ca.allanwang.gitdroid.data.GitCall
import ca.allanwang.gitdroid.data.GitDroidData
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.gitdroid.sql.Database
import com.apollographql.apollo.api.Error
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

@ExperimentalCoroutinesApi
open class BaseViewModel : ViewModel(), KoinComponent {

    val db: Database by inject()
    val gdd: GitDroidData by inject()
    /**
     * Post error
     */
    val gitCallErrors = BroadcastChannel<List<Error>>(Channel.CONFLATED)

    suspend fun <T> GitCall<T>.await(forceRefresh: Boolean): LoadingData<T> =
        with(call(forceRefresh = forceRefresh)) {
            if (errors.isNotEmpty()) {
                L.e { "Error in ${operation.name()}" }
                gitCallErrors.send(errors)
                return@with FailedLoad
            }
            LoadedData(data)
        }

    protected open fun withBundle(bundle: Bundle) {}

    @CheckResult(suggest = "Apply using execute")
    protected fun <T> gitCallLaunch(liveData: LoadingLiveData<T>, call: GitCall<T>): GitCallExecutor =
        object : GitCallExecutor {
            override fun execute(forceRefresh: Boolean) {
                liveData.value = Loading
                L.d { "Post entries" }
                viewModelScope.launch {
                    val result = call.await(forceRefresh)
                    liveData.postValue(result)
                }
            }
        }

    class Factory(val bundle: Bundle?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!BaseViewModel::class.java.isAssignableFrom(modelClass)) {
                throw RuntimeException("Requested view model ${modelClass.simpleName} does not extend ${BaseViewModel::class.java.simpleName}")
            }
            val model = modelClass.newInstance()
            if (model is BaseViewModel && bundle != null) {
                model.withBundle(bundle)
            }
            return model
        }

    }

}


/**
 * Argument corresponds to that of [GitCall]
 */
interface GitCallExecutor {
    fun execute(forceRefresh: Boolean = false)
}


typealias LoadingLiveData<T> = MutableLiveDataKtx<LoadingData<T>>
typealias LoadingListLiveData<T> = LoadingLiveData<List<T>>

sealed class LoadingData<out T>
object Loading : LoadingData<Nothing>()
object FailedLoad : LoadingData<Nothing>()
class LoadedData<T>(val data: T) : LoadingData<T>()

fun <T, R> LoadingData<T>.map(action: (T) -> R): LoadingData<R> = when (this) {
    is Loading -> Loading
    is FailedLoad -> FailedLoad
    is LoadedData<T> -> LoadedData(action(data))
}
