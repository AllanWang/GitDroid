package ca.allanwang.gitdroid.ktx.browser

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import ca.allanwang.gitdroid.ktx.R
import ca.allanwang.kau.utils.resolveColor

fun Context.launchUrl(uri: Uri) {
    val intent = CustomTabsIntent.Builder()
        .setToolbarColor(resolveColor(R.attr.colorPrimary))
        .enableUrlBarHiding()
        .build()
    intent.launchUrl(this, uri)
}