package ca.allanwang.gitdroid.utils

import ca.allanwang.gitdroid.BuildConfig
import ca.allanwang.gitdroid.data.TokenSupplier
import ca.allanwang.kau.kpref.KPref
import ca.allanwang.kau.kpref.KPrefFactory
import ca.allanwang.kau.kpref.KPrefFactoryAndroid
import org.koin.dsl.module

class Prefs(factory: KPrefFactory) : KPref("${BuildConfig.APPLICATION_ID}.prefs", factory) {

    var versionCode: Int by kpref("version_code", -1)

    var prevVersionCode: Int by kpref("prev_version_code", -1)

    var installDate: Long by kpref("install_date", -1L)

    var identifier: Int by kpref("identifier", -1)

    var lastLaunch: Long by kpref("last_launch", -1L)

    var token: String by kpref("token", "")

    companion object {
        fun module() = module {
            single {
                Prefs(KPrefFactoryAndroid(get()))
            }
            single<TokenSupplier> {
                val prefs: Prefs = get()
                object : TokenSupplier {
                    override fun getToken(): String? = prefs.token
                }
            }
        }
    }
}