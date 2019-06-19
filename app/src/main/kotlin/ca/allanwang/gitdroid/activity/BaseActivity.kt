package ca.allanwang.gitdroid.activity

import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import ca.allanwang.gitdroid.BuildConfig
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.data.GitDroidData
import ca.allanwang.gitdroid.ktx.utils.L
import ca.allanwang.gitdroid.sql.Database
import ca.allanwang.gitdroid.sql.awaitOptional
import ca.allanwang.gitdroid.utils.Prefs
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.snackbar
import com.apollographql.apollo.api.Response
import github.sql.GitUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

abstract class BaseActivity : KauBaseActivity() {

    val prefs: Prefs by inject()
    val db: Database by inject()
    val gdd: GitDroidData by inject()

    /**
     * Returns current user based on token
     * If not found, will auto redirect to the login page.
     */
    suspend fun me(): GitUser {
        val me = db.userQueries.select(prefs.token).awaitOptional()
        if (me == null) {
            withContext(Dispatchers.Main) {
                LoginActivity.logout(this@BaseActivity)
            }
            throw  CancellationException("GitUser not found")
        }
        return me
    }

    suspend fun <T> Response<T>.await(): T? {
        errors().also {
            if (it.isNotEmpty()) {
                L.e { "Error in ${operation().name()}" }
                if (BuildConfig.DEBUG) {
                    withContext(Dispatchers.Main) {
                        materialDialog {
                            title(R.string.error)
                            message(text = it.joinToString("\n"))
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        snackbar(R.string.error_occurred)
                    }
                }
            }
        }
        return data()
    }

    fun <T : ViewDataBinding> bindView(
        parent: ViewGroup,
        layoutRes: Int,
        attachToParent: Boolean = true,
        action: T.() -> Unit = {}
    ): T {
        val binding: T = DataBindingUtil.inflate(layoutInflater, layoutRes, parent, attachToParent)
        binding.action()
        return binding
    }

}