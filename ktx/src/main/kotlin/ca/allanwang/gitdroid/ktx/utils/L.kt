package ca.allanwang.gitdroid.ktx.utils

import android.util.Log
import ca.allanwang.gitdroid.ktx.BuildConfig
import ca.allanwang.kau.logging.KauLogger

object L : KauLogger("GitDroid", {
    when (it) {
        Log.VERBOSE -> BuildConfig.DEBUG
        Log.INFO, Log.ERROR -> true
        else -> BuildConfig.DEBUG
    }
}) {

    inline fun test(message: () -> Any?) {
        _d {
            "Test1234 ${message()}"
        }
    }

    inline fun _i(message: () -> Any?) {
        if (BuildConfig.DEBUG)
            i(message)
    }

    inline fun _d(message: () -> Any?) {
        if (BuildConfig.DEBUG)
            d(message)
    }

    inline fun _e(e: Throwable?, message: () -> Any?) {
        if (BuildConfig.DEBUG)
            e(e, message)
    }

//    override fun logImpl(priority: Int, message: String?, t: Throwable?) {
//        if (BuildConfig.DEBUG)
//            super.logImpl(priority, message, t)
//        else {
//            if (message != null)
//                Bugsnag.leaveBreadcrumb(message)
//            if (t != null)
//                Bugsnag.notify(t)
//        }
//    }
}