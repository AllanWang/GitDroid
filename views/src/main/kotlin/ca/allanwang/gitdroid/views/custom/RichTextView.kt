package ca.allanwang.gitdroid.views.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation
import androidx.core.view.doOnPreDraw
import ca.allanwang.gitdroid.views.R

/**
 * TextView wrapper with helper functions for theming and aligning compound drawables
 */
class RichTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val compoundDrawableSize: Int
    private val compoundDrawableGravity: Int
    private val compoundDrawableBounds: Rect?

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.RichTextView, 0, 0)
            .apply {
                try {
                    compoundDrawableSize = getDimensionPixelSize(R.styleable.RichTextView_compoundDrawableSize, -1)
                    compoundDrawableGravity = getInt(R.styleable.RichTextView_compoundDrawableGravity, 0)
                    compoundDrawableBounds =
                        if (compoundDrawableSize == -1) null else Rect(0, 0, compoundDrawableSize, compoundDrawableSize)
                } finally {
                    recycle()
                }
            }
        // Reapply, since first application happened in parent constructor
        // Do in post since start and end drawables don't get registered immediately
        doOnPreDraw {
            val drawables = compoundDrawables
            setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3])
        }
    }

    /**
     * Delegate that passes on values to the original drawable.
     * This allows us to override certain fields as we wish
     */
    private open class DrawableWrapper(val drawable: Drawable) : Drawable() {

        override fun setAlpha(alpha: Int) {
            drawable.alpha = alpha
        }

        override fun getOpacity(): Int {
            return drawable.opacity
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            drawable.colorFilter = colorFilter
        }

        override fun getColorFilter(): ColorFilter? {
            return drawable.colorFilter
        }

        /**
         * We must pass this to super as well since [getBounds] is final
         */
        override fun setBounds(bounds: Rect) {
            super.setBounds(bounds)
            drawable.bounds = bounds
        }

        override fun draw(canvas: Canvas) {
            drawable.draw(canvas)
        }

        override fun setTintList(tint: ColorStateList?) {
            drawable.setTintList(tint)
        }


        override fun setTint(tintColor: Int) {
            drawable.setTint(tintColor)
        }

        override fun setTintMode(tintMode: PorterDuff.Mode) {
            drawable.setTintMode(tintMode)
        }

        override fun getIntrinsicWidth(): Int {
            return drawable.intrinsicWidth
        }

        override fun getIntrinsicHeight(): Int {
            return drawable.intrinsicHeight
        }
    }

    private class TopDrawable(drawable: Drawable, val paint: TextPaint) : DrawableWrapper(drawable) {

        @SuppressLint("CanvasSize")
        override fun draw(canvas: Canvas) {
            val lineHeight = paint.fontMetrics.bottom - paint.fontMetrics.top
            canvas.withTranslation(y = (lineHeight - canvas.height) / 2f) {
                super.draw(canvas)
            }
        }

    }

    private fun Drawable.orig() = if (this is DrawableWrapper) drawable else this
    private fun Drawable.topDrawable() = this as? TopDrawable ?: TopDrawable(this, paint)

    private fun update(drawable: Drawable): Drawable {
        val d = when (compoundDrawableGravity) {
            0 -> drawable.orig()
            1 -> drawable.topDrawable()
            else -> throw RuntimeException("Unknown drawable gravity flag $compoundDrawableGravity")
        }
        compoundDrawableBounds?.also {
            d.bounds = it
        }
        return d
    }

    override fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawables(
            left?.let(this::update),
            top?.let(this::update),
            right?.let(this::update),
            bottom?.let(this::update)
        )
    }
}