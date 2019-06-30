package ca.allanwang.gitdroid.activity.base

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import ca.allanwang.gitdroid.data.GitNameAndOwner
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.data.GitRef
import ca.allanwang.gitdroid.logger.L
import kotlin.reflect.KProperty

abstract class IntentActivity : BaseActivity() {

    private val requiredExtras: MutableList<String> = mutableListOf()
    private val intentDelegates: MutableList<IntentDelegate<*>> = mutableListOf()

    override fun setIntent(newIntent: Intent?) {
        verifyExtras(newIntent)
        super.setIntent(newIntent)
    }

    protected fun repoExtra() = extra<GitNameAndOwner> { repo }
    protected fun oidExtra() = extra<GitObjectID> { oid }
    protected fun refExtraOptional() = extra<GitRef?> { ref }

    protected fun <T : Parcelable?> extra(key: Args.() -> String) =
        intentDelegate(Args.key()) { getParcelableExtra<T>(it) }

    protected fun stringExtra(key: Args.() -> String) =
        intentDelegate(Args.key()) { getStringExtra(it)!! }

    protected fun intExtra(key: Args.() -> String) =
        intentDelegate(Args.key()) { getIntExtra(it, 0) }

    private fun <T> intentDelegate(
        key: String,
        getter: Intent.(key: String) -> T
    ): IntentDelegate<T> {
        requiredExtras.add(key)
        val delegate =  IntentDelegate(key, getter)
        intentDelegates.add(delegate)
        return delegate
    }

    private object UNINITIALIZED

    /**
     * Standard variable with a default fetcher for intents.
     * Can be reset when a new intent arrives
     */
    protected inner class IntentDelegate<T>(
        val key: String,
        val getter: Intent.(key: String) -> T
    ) {
        private var _value: Any? = UNINITIALIZED

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            _value = value
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (_value === UNINITIALIZED) {
                _value = intent!!.getter(key)
            }
            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

        fun reset() {
            _value = UNINITIALIZED
        }
    }

    private fun verifyExtras(intent: Intent?) {
        fun fail(message: () -> Any?) {
            L.fail(message)
            finish()
        }

        intentDelegates.forEach { it.reset() }

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

    object Args {
        private const val TAG = "gitdroid_arg"

        const val login = "$TAG-login"
        const val repo = "$TAG-repo"
        const val issueNumber = "$TAG-issue-number"
        const val name = "$TAG-name"
        const val oid = "$TAG-oid"
        const val ref = "$TAG-ref"
    }

}