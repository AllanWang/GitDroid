package ca.allanwang.gitdroid.activity.base

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import ca.allanwang.gitdroid.data.GitObjectID
import ca.allanwang.gitdroid.logger.L
import ca.allanwang.gitdroid.views.GitNameAndOwner
import kotlin.reflect.KProperty

abstract class IntentActivity : BaseActivity() {

    private val requiredExtras: MutableList<String> = mutableListOf()

    override fun setIntent(newIntent: Intent?) {
        verifyExtras(newIntent)
        super.setIntent(newIntent)
    }

    protected fun repoExtra() = parcelableExtra<GitNameAndOwner> { repo }
    protected fun oidExtra() = parcelableExtra<GitObjectID> { oid }

    protected fun <T : Parcelable> parcelableExtra(key: Args.() -> String) =
        intentDelegate(Args.key()) { getParcelableExtra<T>(it)!! }

    protected fun stringExtra(key: Args.() -> String) = intentDelegate(Args.key()) { getStringExtra(it)!! }
    protected fun intExtra(key: Args.() -> String) = intentDelegate(Args.key()) { getIntExtra(it, 0) }

    private fun <T> intentDelegate(key: String, getter: Intent.(key: String) -> T): IntentDelegate<T> {
        requiredExtras.add(key)
        return IntentDelegate(key, getter)
    }

    protected inner class IntentDelegate<T>(val key: String, val getter: Intent.(key: String) -> T) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T = intent!!.getter(key)
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

    object Args {
        private const val TAG = "gitdroid_arg"

        const val login = "$TAG-login"
        const val repo = "$TAG-repo"
        const val issueNumber = "$TAG-issue-number"
        const val name = "$TAG-name"
        const val oid = "$TAG-oid"
    }

}