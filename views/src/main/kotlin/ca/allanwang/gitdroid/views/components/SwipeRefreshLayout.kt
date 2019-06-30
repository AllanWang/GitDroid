package ca.allanwang.gitdroid.views.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * Variant that forbids refreshing if child layout is not at the top
 * Inspired by https://github.com/slapperwan/gh4a/blob/master/app/src/main/java/com/gh4a/widget/SwipeRefreshLayout.java
 *
 */
class SwipeRefreshLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SwipeRefreshLayout(context, attrs) {

    private var preventRefresh: Boolean = false
    private var downY: Float = -1f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action != MotionEvent.ACTION_DOWN && preventRefresh) {
            return false
        }
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downY = ev.y
                preventRefresh = canChildScrollUp()
            }
            MotionEvent.ACTION_MOVE -> {
                if (downY - ev.y > touchSlop) {
                    preventRefresh = true
                    return false
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        if (preventRefresh) {
            /*
             * Ignoring offsetInWindow since
             * 1. It doesn't seem to matter in the typical use case
             * 2. It isn't being transferred to the underlying array used by the super class
             */
            dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null)
        } else {
            super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        }
    }

    interface OnRefreshListener : SwipeRefreshLayout.OnRefreshListener

}