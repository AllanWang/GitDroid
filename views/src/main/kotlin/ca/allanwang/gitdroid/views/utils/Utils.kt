package ca.allanwang.gitdroid.views.utils

import android.content.Context
import android.text.format.DateFormat
import androidx.annotation.PluralsRes
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

fun Locale.bestDateFormat(skeleton: String): SimpleDateFormat =
    SimpleDateFormat(DateFormat.getBestDateTimePattern(this, skeleton), this)

fun Context.quantityN(@PluralsRes res: Int, n: Int): String =
    resources.getQuantityString(res, n, n)

fun <T> List<T>.repeat(n: Int): List<T> = generateSequence { this }.take(n).flatten().toList()


/**
 * Lazy without thread safety
 */
fun <T> lazyUi(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

var RecyclerView.fastAdapter: FastBindingAdapter
    set(value) {
        adapter = value
    }
    get() = adapter as? FastBindingAdapter
        ?: throw RuntimeException("${FastBindingAdapter::class.java.simpleName} not bound to recyclerview")