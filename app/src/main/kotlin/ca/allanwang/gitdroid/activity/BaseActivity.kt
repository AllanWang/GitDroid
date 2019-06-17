package ca.allanwang.gitdroid.activity

import ca.allanwang.gitdroid.data.GitDroidData
import ca.allanwang.gitdroid.sql.Database
import ca.allanwang.gitdroid.sql.awaitOptional
import ca.allanwang.gitdroid.utils.Prefs
import ca.allanwang.kau.internal.KauBaseActivity
import github.sql.GitUser
import org.koin.android.ext.android.inject

abstract class BaseActivity : KauBaseActivity() {

    protected val prefs: Prefs by inject()
    protected val db: Database by inject()
    protected val gdd: GitDroidData by inject()

    suspend fun me(): GitUser? {
        val me = db.userQueries.select(prefs.token).awaitOptional()
        if (me == null) {
            LoginActivity.logout(this)
        }
        return me
    }

}