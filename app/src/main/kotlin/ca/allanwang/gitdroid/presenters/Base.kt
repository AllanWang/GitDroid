package ca.allanwang.gitdroid.presenters

import android.content.Context
import android.os.Bundle
import androidx.databinding.ViewDataBinding
import ca.allanwang.gitdroid.data.GitCall
import ca.allanwang.gitdroid.data.GitDroidData
import ca.allanwang.gitdroid.sql.Database
import ca.allanwang.kau.utils.ContextHelper
import github.sql.GitUser
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

interface PresenterContext {
    val context: Context
    val gdd: GitDroidData
    val db: Database
    /**
     * Returns current user based on token
     * If not found, will auto redirect to the login page.
     */
    suspend fun me(): GitUser

    /**
     * Get call data, cancelling if an error occurred or if null data was received
     */
    suspend fun <T> GitCall<T>.await(forceRefresh: Boolean = false): T
}

abstract class BasePresenter(presenterContext: PresenterContext) : CoroutineScope,
    PresenterContext by presenterContext {

    /**
     * Unique tag per presenter.
     * Also used for bundle flags if necessary.
     */
    open val tag: String = this::class.java.name
    abstract val binding: ViewDataBinding

    open lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = ContextHelper.dispatcher + job + CoroutineName(tag)

    init {
        start()
    }

    fun start() {
        job = SupervisorJob()
    }

    fun cancel() {
        job.cancel()
    }

    open fun onRestoreInstanceState(savedInstanceState: Bundle) {}

    open fun onSaveInstanceState(outState: Bundle) {}

    /**
     * Called when a back press event is received.
     * Return true if it is handled
     */
    open fun onBackPressed() = false

}