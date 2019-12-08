package ca.allanwang.gitdroid.utils

import android.content.Context
import ca.allanwang.gitdroid.data.TokenSupplier
import ca.allanwang.kau.kpref.KPref
import org.koin.dsl.module

class Prefs : KPref() {

    var versionCode: Int by kpref("version_code", -1)

    var prevVersionCode: Int by kpref("prev_version_code", -1)

    var installDate: Long by kpref("install_date", -1L)

    var identifier: Int by kpref("identifier", -1)

    var lastLaunch: Long by kpref("last_launch", -1L)

    var token: String by kpref("token", "")

    companion object {
        fun module(context: Context, name: String) = module {
            single {
                val prefs = Prefs()
                prefs.initialize(context, name)
                prefs
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