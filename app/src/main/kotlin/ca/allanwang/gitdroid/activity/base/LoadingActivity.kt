package ca.allanwang.gitdroid.activity.base

import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import ca.allanwang.gitdroid.logger.L
import kotlin.reflect.KProperty

abstract class LoadingActivity<Binding : ViewDataBinding> : BaseActivity() {

    lateinit var binding: Binding

    abstract val layoutRes: Int
        @LayoutRes get

    private val requiredExtras: MutableList<String> = mutableListOf()

    override fun setIntent(newIntent: Intent?) {
        missingExtras(intent)
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

    private fun missingExtras(intent: Intent?): Boolean {
        if (requiredExtras.isEmpty()) {
            return false
        }
        if (intent == null) {
            L.fail { "Required extras $requiredExtras, but intent is null" }
            finish()
            return true
        }
        val missing = requiredExtras.filter { !intent.hasExtra(it) }
        if (missing.isEmpty()) {
            return false
        }
        L.fail { "Missing extras $missing" }
        finish()
        return true
    }

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (missingExtras(intent)) {
            return
        }
        binding = bindContentView(layoutRes)
        onCreate2(savedInstanceState)
    }

    abstract fun onCreate2(savedInstanceState: Bundle?)

}