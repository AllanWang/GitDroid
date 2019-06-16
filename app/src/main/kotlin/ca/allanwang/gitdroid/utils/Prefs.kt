package ca.allanwang.gitdroid.utils

import ca.allanwang.gitdroid.data.TokenSupplier
import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.kpref
import org.koin.dsl.module

object Prefs : KPref() {

    var versionCode: Int by kpref("version_code", -1)

    var prevVersionCode: Int by kpref("prev_version_code", -1)

    var installDate: Long by kpref("install_date", -1L)

    var identifier: Int by kpref("identifier", -1)

    var lastLaunch: Long by kpref("last_launch", -1L)

    var token: String by kpref("token", "")

    fun tokenModule() = module {
        single<TokenSupplier> {
            object : TokenSupplier {
                override fun getToken(): String? = token
            }
        }
    }
}