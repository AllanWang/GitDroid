package ca.allanwang.gitdroid.views.itemdecoration

import android.annotation.SuppressLint
import android.content.Context
import ca.allanwang.gitdroid.views.R
import ca.allanwang.kau.utils.dimenPixelSize

@SuppressLint("PrivateResource")
class BottomNavDecoration(context: Context) :
    MarginDecoration(marginBottom = context.dimenPixelSize(R.dimen.design_bottom_navigation_height))