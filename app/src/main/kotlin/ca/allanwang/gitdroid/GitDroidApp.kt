package ca.allanwang.gitdroid

import android.app.Application
import ca.allanwang.gitdroid.data.GitDroidData
import ca.allanwang.gitdroid.sql.Database
import ca.allanwang.gitdroid.sql.GitDb
import ca.allanwang.gitdroid.utils.Prefs
import ca.allanwang.kau.logging.KL
import com.squareup.sqldelight.android.AndroidSqliteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import kotlin.random.Random

class GitDroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        KL.shouldLog = { BuildConfig.DEBUG }
        Prefs.initialize(this, "${BuildConfig.APPLICATION_ID}.prefs")
        if (Prefs.installDate == -1L) Prefs.installDate = System.currentTimeMillis()
        if (Prefs.identifier == -1) Prefs.identifier = Random.nextInt(Int.MAX_VALUE)
        Prefs.lastLaunch = System.currentTimeMillis()
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@GitDroidApp)
            modules(
                listOf(
                    Prefs.tokenModule(),
                    GitDroidData.module(),
                    GitDb.module(AndroidSqliteDriver(Database.Schema, this@GitDroidApp, "gitdroid.db"))
                )
            )
        }
    }
}