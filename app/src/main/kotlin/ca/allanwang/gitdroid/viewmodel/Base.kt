package ca.allanwang.gitdroid.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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

    suspend fun <T> GitCall<T>.await(forceRefresh: Boolean): LoadingData<T?> =
        with(call(forceRefresh = forceRefresh)) {
            errors().also {
                if (it.isNotEmpty()) {
                    L.e { "Error in ${operation().name()}" }
                    gitCallErrors.send(it)
                    return@with FailedLoad
                }
            }
            LoadedData(data())
        }

    protected fun <T> gitCallLaunch(liveData: LoadingLiveData<T?>, call: GitCall<T>): GitCallExecutor =
        object : GitCallExecutor {
            override fun execute(forceRefresh: Boolean) {
                liveData.value = Loading
                viewModelScope.launch {
                    val result = call.await(forceRefresh)
                    liveData.postValue(result)
                }
            }
        }


    protected fun <T> gitCallListLaunch(liveData: LoadingLiveData<List<T>>, call: GitCall<List<T>>): GitCallExecutor =
        object : GitCallExecutor {
            override fun execute(forceRefresh: Boolean) {
                liveData.value = Loading
                viewModelScope.launch {
                    val result = call.await(forceRefresh).map { it ?: emptyList() }
                    liveData.postValue(result)
                }
            }
        }
}


/**
 * Argument corresponds to that of [GitCall]
 */
interface GitCallExecutor {
    fun execute(forceRefresh: Boolean = false)
}


typealias LoadingLiveData<T> = MutableLiveData<LoadingData<T>>
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
