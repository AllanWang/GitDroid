package ca.allanwang.gitdroid.activity

import ca.allanwang.gitdroid.data.GitDroidData
import ca.allanwang.gitdroid.sql.Database
import ca.allanwang.gitdroid.utils.Prefs
import ca.allanwang.kau.internal.KauBaseActivity
import org.koin.android.ext.android.inject

abstract class BaseActivity : KauBaseActivity() {

    protected val prefs: Prefs by inject()
    protected val db: Database by inject()
    protected val gdd: GitDroidData by inject()

}