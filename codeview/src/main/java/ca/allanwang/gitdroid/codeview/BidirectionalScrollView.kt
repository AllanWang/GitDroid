package ca.allanwang.gitdroid.codeview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.OverScroller
import ca.allanwang.gitdroid.logger.L

/**
 * Scrollview that handles both x and y scrolling
 *
 * Name inspired by
 * https://github.com/kbiakov/CodeView-Android/blob/master/codeview/src/main/java/io/github/kbiakov/codeview/views/BidirectionalScrollView.kt
 *
 * This class is primarily a copy of [HorizontalScrollView], where functions will handle the y axis as well.
 * Clamping is removed in some locations to ensure flinging capabilities when an edge is hit
 */
class BidirectionalScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var lastMotionX = 0f
    private var lastMotionY = 0f
    private var dragged = false
    private val scroller = OverScroller(context)
    private val touchSlop: Int
    private val minVelocity: Float
    private val maxVelocity: Float
    private val horizontalScrollFactor: Float
    private val verticalScrollFactor: Float
    private var activePointerId: Int = INVALID_POINTER
    private var _velocityTracker: VelocityTracker? = null
    private val velocityTracker: VelocityTracker
        @SuppressLint("Recycle")
        get() = _velocityTracker ?: VelocityTracker.obtain().also { _velocityTracker = it }
    private val overscrollDistance: Int
    private val overflingDistance: Int

    companion object {
        private const val INVALID_POINTER = -1
    }

    init {
        overScrollMode = View.OVER_SCROLL_ALWAYS
        with(ViewConfiguration.get(context)) {
            touchSlop = scaledTouchSlop
            minVelocity = scaledMinimumFlingVelocity.toFloat()
            maxVelocity = scaledMaximumFlingVelocity.toFloat()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                horizontalScrollFactor = scaledHorizontalScrollFactor
                verticalScrollFactor = scaledVerticalScrollFactor
            } else {
                horizontalScrollFactor = 1f
                verticalScrollFactor = 1f
            }
            overscrollDistance = scaledOverscrollDistance
            overflingDistance = scaledOverflingDistance
        }
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) {
            recycleVelocityTracker()
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_MOVE && dragged) {
            return true
        }
        if (super.onInterceptTouchEvent(ev)) {
            return true
        }
        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = pointerIndex(ev)
                if (pointerIndex != -1) {
                    val x = ev.getX(pointerIndex)
                    val y = ev.getY(pointerIndex)
                    if (Math.abs(lastMotionX - x) > touchSlop || Math.abs(lastMotionY - y) > touchSlop) {
                        dragged = true
                        lastMotionX = x
                        lastMotionY = y
                        velocityTracker.addMovement(ev)
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                lastMotionX = x
                lastMotionY = y
                activePointerId = ev.getPointerId(0)
                _velocityTracker?.clear()
                velocityTracker.addMovement(ev)
                dragged = scroller.isFinished
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                dragged = false
                activePointerId = INVALID_POINTER
                springBack()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                lastMotionX = ev.getX(index)
                lastMotionY = ev.getY(index)
                activePointerId = index
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
            }
        }
        return dragged
    }

    private fun pointerIndex(ev: MotionEvent): Int {
        if (activePointerId == INVALID_POINTER) {
            return -1
        }
        return ev.findPointerIndex(activePointerId)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        velocityTracker.addMovement(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (childCount == 0) {
                    return false
                }
                dragged = !scroller.isFinished
                if (!scroller.isFinished) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                    scroller.abortAnimation()
                }
                lastMotionX = ev.x
                lastMotionY = ev.y
                activePointerId = ev.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = pointerIndex(ev)
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)
                var dx = lastMotionX - x
                var dy = lastMotionY - y
                if (!dragged) {
                    if (Math.abs(dx) > touchSlop) {
                        dragged = true
                        if (dx > 0) {
                            dx -= touchSlop
                        } else {
                            dx += touchSlop
                        }
                    }
                    if (Math.abs(dy) > touchSlop) {
                        dragged = true
                        if (dy > 0) {
                            dy -= touchSlop
                        } else {
                            dy += touchSlop
                        }
                    }
                    if (dragged) {
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                if (dragged) {
                    lastMotionX = x
                    lastMotionY = y
                    val oldX = scrollX
                    val oldY = scrollY
                    val rangeX = scrollRangeX
                    val rangeY = scrollRangeY
                    val overscrollMode = overScrollMode
                    val canOverscroll =
                        overscrollMode == View.OVER_SCROLL_ALWAYS
                                || (overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS
                                && (rangeX > 0 || rangeY > 0))
                    // TODO verify
                    // before, velocity was cleared
                    // may have effect on edge views
                    overScrollBy(
                        dx.toInt(), dy.toInt(),
                        scrollX, scrollY,
                        rangeX, rangeY,
                        overscrollDistance, overscrollDistance, true
                    )
                    if (canOverscroll) {
                        val pulledToX = oldX + dx
                        val pulledToY = oldY + dy
                        // TODO add edge glow
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (dragged) {
                    val velocityTracker = velocityTracker
                    velocityTracker.computeCurrentVelocity(1000, maxVelocity)
                    val initialVelocityX = velocityTracker.getXVelocity(activePointerId)
                    val initialVelocityY = velocityTracker.getYVelocity(activePointerId)

                    L.d { "Fling request $initialVelocityX $initialVelocityY $minVelocity" }
                    if (childCount > 0) {
                        if (Math.abs(initialVelocityX) > minVelocity || Math.abs(initialVelocityY) > minVelocity) {
                            fling(-initialVelocityX.toInt(), -initialVelocityY.toInt())
                        } else {
                            springBack()
                        }
                    }

                    activePointerId = INVALID_POINTER
                    dragged = false
                    recycleVelocityTracker()

                    // TODO update edge glow
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (dragged && childCount > 0) {
                    springBack()
                    activePointerId = INVALID_POINTER
                    dragged = false
                    recycleVelocityTracker()

                    // TODO update edge glow
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
            }
        }
        return true
    }

    private fun springBack() {
        if (scroller.springBack(scrollX, scrollY, 0, scrollRangeX, 0, scrollRangeY)) {
            postInvalidateOnAnimation()
        }
    }

    private fun fling(velocityX: Int, velocityY: Int) {
        if (childCount == 0) {
            return
        }
        val child = getChildAt(0)
        val width = width - paddingLeft - paddingRight
        val height = height - paddingTop - paddingBottom
        val right = child.width
        val bottom = child.height
        scroller.fling(
            scrollX,
            scrollY,
            velocityX,
            velocityY,
            0,
            Math.max(0, right - width),
            0,
            Math.max(0, bottom - height),
            width / 2,
            height / 2
        )
        val movingRight = velocityX > 0
        val movingBottom = velocityY > 0
        // todo add focus change?
        postInvalidateOnAnimation()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex =
            ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == activePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            activePointerId = ev.getPointerId(newPointerIndex)
            lastMotionX = ev.getX(newPointerIndex)
            lastMotionY = ev.getY(newPointerIndex)
            _velocityTracker?.clear()
        }
    }

    private val scrollRangeX: Int
        get() {
            if (childCount > 0) {
                val child = getChildAt(0)
                return Math.max(0, child.width - (width - paddingLeft - paddingRight))
            }
            return 0
        }

    private val scrollRangeY: Int
        get() {
            if (childCount > 0) {
                val child = getChildAt(0)
                return Math.max(0, child.height - (height - paddingTop - paddingBottom))
            }
            return 0
        }

    // TODO add saved state?

    private fun recycleVelocityTracker() {
        _velocityTracker?.recycle()
        _velocityTracker = null
    }

    override fun scrollTo(x: Int, y: Int) {
        if (childCount == 0) {
            return
        }
        val child = getChildAt(0)
        val cx = clamp(x, width - paddingLeft - paddingRight, child.width)
        val cy = clamp(y, height - paddingTop - paddingBottom, child.height)
        if (cx != scrollX || cy != scrollY) {
            super.scrollTo(cx, cy)
        }
    }

    private fun clamp(n: Int, my: Int, child: Int): Int {
        if (my >= child || n < 0) {
            return 0
        }
        return if (my + n > child) {
            child - my
        } else n
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        // Treat animating scrolls differently; see #computeScroll() for why.
        if (!scroller.isFinished) {
            val oldX = this.scrollX
            val oldY = this.scrollY
            this.scrollX = scrollX
            this.scrollY = scrollY
            // invalidateParentIfNeeded()
            onScrollChanged(scrollX, scrollY, oldX, oldY)
        } else {
            super.scrollTo(scrollX, scrollY)
        }
        awakenScrollBars()
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        val rangeX = scrollRangeX
        val rangeY = scrollRangeY
        event.isScrollable = scrollRangeX > 0 || scrollRangeY > 0
        event.scrollX = scrollX
        event.scrollY = scrollY
        event.maxScrollX = rangeX
        event.maxScrollY = rangeY
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            // This is called at drawing time by ViewGroup.  We don't want to
            // re-show the scrollbars at this point, which scrollTo will do,
            // so we replicate most of scrollTo here.
            //
            //         It's a little odd to call onScrollChanged from inside the drawing.
            //
            //         It is, except when you remember that computeScroll() is used to
            //         animate scrolling. So unless we want to defer the onScrollChanged()
            //         until the end of the animated scrolling, we don't really have a
            //         choice here.
            //
            //         I agree.  The alternative, which I think would be worse, is to post
            //         something and tell the subclasses later.  This is bad because there
            //         will be a window where scrollX/Y is different from what the app
            //         thinks it is.
            //
            val oldX = scrollX
            val oldY = scrollY
            val x = scroller.currX
            val y = scroller.currY

            if (oldX != x || oldY != y) {
                val rangeX = scrollRangeX
                val rangeY = scrollRangeY
                val overscrollMode = overScrollMode
                val canOverscroll =
                    overscrollMode == View.OVER_SCROLL_ALWAYS || overscrollMode == View.OVER_SCROLL_IF_CONTENT_SCROLLS && (rangeX > 0 || rangeY > 0)

                overScrollBy(
                    x - oldX, y - oldY, oldX, oldY, rangeX, rangeY,
                    overflingDistance, overflingDistance, false
                )
                onScrollChanged(scrollX, scrollY, oldX, oldY)

//                if (canOverscroll) {
//                    if (x < 0 && oldX >= 0) {
//                        mEdgeGlowLeft.onAbsorb(scroller.getCurrVelocity().toInt())
//                    } else if (x > range && oldX <= range) {
//                        mEdgeGlowRight.onAbsorb(scroller.getCurrVelocity().toInt())
//                    }
//                }
            }

            if (!awakenScrollBars()) {
                postInvalidateOnAnimation()
            }
        }
    }

    override fun measureChild(child: View, parentWidthMeasureSpec: Int, parentHeightMeasureSpec: Int) {
        val zeroMeasureSpec = makeMeasureSpec(0)
        child.measure(zeroMeasureSpec, zeroMeasureSpec)
    }

    override fun measureChildWithMargins(
        child: View,
        parentWidthMeasureSpec: Int, widthUsed: Int,
        parentHeightMeasureSpec: Int, heightUsed: Int
    ) = with(child.layoutParams as MarginLayoutParams) {
        val widthMeasureSpec = makeMeasureSpec(leftMargin + rightMargin)
        val heightMeasureSpec = makeMeasureSpec(topMargin + bottomMargin)
        child.measure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun makeMeasureSpec(size: Int) = MeasureSpec.makeMeasureSpec(size, MeasureSpec.UNSPECIFIED)

}
