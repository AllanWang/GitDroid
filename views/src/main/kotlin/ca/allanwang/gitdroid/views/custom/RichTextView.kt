package ca.allanwang.gitdroid.views.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.graphics.withTranslation
import androidx.core.view.doOnPreDraw
import ca.allanwang.gitdroid.views.R

/**
 * TextView wrapper with helper functions for theming and aligning compound drawables
 */
class RichTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    private val compoundDrawableTint: Int?
    private val compoundDrawableSize: Int
    private val compoundDrawableGravity: Int
    private val compoundDrawableBounds: Rect?

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.RichTextView, 0, 0)
            .apply {
                try {
                    compoundDrawableSize = getDimensionPixelSize(R.styleable.RichTextView_compoundDrawableSize, -1)
                    compoundDrawableTint = if (hasValue(R.styleable.RichTextView_compoundDrawableTint)) getColor(
                        R.styleable.RichTextView_compoundDrawableTint,
                        0
                    ) else null
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
            // L.d { "NNN ${drawables.any { it != null }}" }
            setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3])
        }
    }

    private interface DrawableWrapper {
        val drawable: Drawable
    }

    private class TopDrawable(override val drawable: Drawable, val paint: TextPaint) : Drawable(), DrawableWrapper {

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

        @SuppressLint("CanvasSize")
        override fun draw(canvas: Canvas) {
            val lineHeight = paint.fontMetrics.bottom - paint.fontMetrics.top
            canvas.withTranslation(y = (lineHeight - canvas.height) / 2f) {
                drawable.draw(canvas)
            }
        }

        override fun setBounds(bounds: Rect) {
            super.setBounds(bounds)
            drawable.bounds = bounds
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
        if (compoundDrawableBounds != null) {
            d.bounds = compoundDrawableBounds
        }
        if (compoundDrawableTint != null) {
            d.setTint(compoundDrawableTint)
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