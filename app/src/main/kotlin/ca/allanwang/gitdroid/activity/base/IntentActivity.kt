package ca.allanwang.gitdroid.activity.base

import android.content.Intent
import android.os.Bundle
import ca.allanwang.gitdroid.logger.L
import kotlin.reflect.KProperty

abstract class IntentActivity : BaseActivity() {

    private val requiredExtras: MutableList<String> = mutableListOf()

    override fun setIntent(newIntent: Intent?) {
        verifyExtras(newIntent)
        super.setIntent(newIntent)
    }

    protected fun stringExtra(key: String) = intentDelegate(key) { getStringExtra(key)!! }
    protected fun intExtra(key: String) = intentDelegate(key) { getIntExtra(key, 0) }

    private fun <T> intentDelegate(key: String, getter: Intent.() -> T): IntentDelegate<T> {
        requiredExtras.add(key)
        return IntentDelegate(getter)
    }

    protected inner class IntentDelegate<T>(val getter: Intent.() -> T) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T = intent!!.getter()
    }

    private fun verifyExtras(intent: Intent?) {
        fun fail(message: () -> Any?) {
            L.fail(message)
            finish()
        }

        if (requiredExtras.isEmpty()) {
            return
        }
        if (intent == null) {
            return fail { "Required extras $requiredExtras, but intent is null" }
        }
        val missing = requiredExtras.filter { !intent.hasExtra(it) }
        if (missing.isNotEmpty()) {
            return fail { "Missing extras $missing" }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        verifyExtras(intent)
    }

}