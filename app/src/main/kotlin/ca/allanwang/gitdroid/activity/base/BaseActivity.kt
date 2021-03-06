package ca.allanwang.gitdroid.activity.base

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ca.allanwang.gitdroid.BuildConfig
import ca.allanwang.gitdroid.R
import ca.allanwang.gitdroid.activity.LoginActivity
import ca.allanwang.gitdroid.activity.MainActivity
import ca.allanwang.gitdroid.data.gql.GitCall
import ca.allanwang.gitdroid.data.GitDroidData
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.gitdroid.sql.Database
import ca.allanwang.gitdroid.sql.awaitOptional
import ca.allanwang.gitdroid.utils.Prefs
import ca.allanwang.gitdroid.viewmodel.base.BaseViewModel
import ca.allanwang.kau.internal.KauBaseActivity
import ca.allanwang.kau.utils.materialDialog
import ca.allanwang.kau.utils.snackbar
import ca.allanwang.kau.utils.startActivity
import github.sql.GitUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

abstract class BaseActivity : KauBaseActivity() {

    val prefs: Prefs by inject()
    val db: Database by inject()
    val gdd: GitDroidData by inject()

    fun <T : ViewDataBinding> bindContentView(@LayoutRes layoutRes: Int): T =
        DataBindingUtil.setContentView(this, layoutRes)

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

    /**
     * Get call data, cancelling if an error occurred or if null data was received
     */
    suspend fun <T> GitCall<T>.await(forceRefresh: Boolean = true): T {
        return with(call(forceRefresh = forceRefresh)) {
            errors.also {
                if (it.isNotEmpty()) {
                    L.e { "Error in ${operation.name()}" }
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
                    throw CancellationException("Error in ${operation.name()}")
                }
            }
            data.let {
                if (it == null) {
                    withContext(Dispatchers.Main) {
                        snackbar(R.string.error_not_found) // todo this isn't really an error
                    }
                    throw CancellationException(("Null data received"))
                }
                it
            }
        }
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

    inline fun <reified T : ViewModel> viewModel(factory: ViewModelProvider.Factory? = BaseViewModel.Factory(intent.extras)) =
        ViewModelProviders.of(this, factory).get(T::class.java)

    fun inflateMenu(@MenuRes menuRes: Int, menu: Menu) {
        val tintList = ColorStateList.valueOf(Color.WHITE)
        menuInflater.inflate(menuRes, menu)
        // TODO tint for other versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            menu.forEach {
                it.iconTintList = tintList
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (BuildConfig.DEBUG) startActivity<MainActivity>() else onBackPressed()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

}