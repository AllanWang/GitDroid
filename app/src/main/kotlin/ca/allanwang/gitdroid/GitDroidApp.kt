package ca.allanwang.gitdroid

import android.app.Application
import ca.allanwang.gitdroid.sql.Db
import ca.allanwang.gitdroid.utils.Prefs
import ca.allanwang.kau.logging.KL
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
        Db.initialize(this, "gitdroid.db")
        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@GitDroidApp)
            modules(Prefs.tokenModule())
        }
    }
}