package ca.allanwang.gitdroid

import android.app.Application
import ca.allanwang.gitdroid.data.GitDroidData
import ca.allanwang.gitdroid.sql.Database
import ca.allanwang.gitdroid.sql.GitDb
import ca.allanwang.gitdroid.utils.Prefs
import ca.allanwang.kau.logging.KL
import com.squareup.sqldelight.android.AndroidSqliteDriver
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import kotlin.random.Random

class GitDroidApp : Application() {

    private val prefs: Prefs by inject()

    override fun onCreate() {
        super.onCreate()
        KL.shouldLog = { BuildConfig.DEBUG }
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@GitDroidApp)
            modules(
                listOf(
                    Prefs.module(this@GitDroidApp, "${BuildConfig.APPLICATION_ID}.prefs"),
                    GitDroidData.module(),
                    GitDb.module(AndroidSqliteDriver(Database.Schema, this@GitDroidApp, "gitdroid.db"))
                )
            )
        }
        if (prefs.installDate == -1L) prefs.installDate = System.currentTimeMillis()
        if (prefs.identifier == -1) prefs.identifier = Random.nextInt(Int.MAX_VALUE)
        prefs.lastLaunch = System.currentTimeMillis()
    }
}