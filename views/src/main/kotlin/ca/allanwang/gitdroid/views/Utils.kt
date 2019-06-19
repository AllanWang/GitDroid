package ca.allanwang.gitdroid.views

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun Locale.bestDateFormat(skeleton: String): SimpleDateFormat =
    SimpleDateFormat(DateFormat.getBestDateTimePattern(this, skeleton), this)