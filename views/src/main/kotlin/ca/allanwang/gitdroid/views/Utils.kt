package ca.allanwang.gitdroid.views

import android.content.Context
import android.text.format.DateFormat
import androidx.annotation.PluralsRes
import java.text.SimpleDateFormat
import java.util.*

fun Locale.bestDateFormat(skeleton: String): SimpleDateFormat =
    SimpleDateFormat(DateFormat.getBestDateTimePattern(this, skeleton), this)

fun Context.quantityN(@PluralsRes res: Int, n: Int): String =
    resources.getQuantityString(res, n, n)

fun <T> List<T>.repeat(n: Int): List<T> = generateSequence { this }.take(n).flatten().toList()