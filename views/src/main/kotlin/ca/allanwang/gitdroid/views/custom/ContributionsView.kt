package ca.allanwang.gitdroid.views.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.getColorOrThrow
import androidx.databinding.BindingAdapter
import ca.allanwang.gitdroid.views.R
import github.fragment.ShortContributions

@BindingAdapter("contributions")
fun ContributionsView.contributions(data: ShortContributions?) {
    contributions = data
}

class ContributionsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var colorPaints: Array<Paint> = emptyArray()
    private val cellBorder: Float
    private val baseColor: Int

    private inline val Int.luminance: Int
        get() = (0.299f * Color.red(this) + 0.587f * Color.green(this) + 0.114f * Color.blue(this)).toInt()

    init {
        setWillNotDraw(false)
        context.theme.obtainStyledAttributes(attrs, R.styleable.ContributionsView, 0, 0)
            .apply {
                try {
                    cellBorder = getDimension(R.styleable.ContributionsView_contributionCellBorder, 0f)
                    baseColor = getColorOrThrow(R.styleable.ContributionsView_contributionColor)
                } finally {
                    recycle()
                }
            }
    }

    var contributions: ShortContributions? = null
        set(value) {
            field = value
            onNewData(value)
        }
    private var points: Array<IntArray> = emptyArray()
    private var cellSize: Int = 0

    private fun cellPaint(color: Int, alpha: Int) = Paint().also {
        it.isAntiAlias = true
        it.style = Paint.Style.FILL
        it.color = color
        // Order matters; place alpha below color
        it.alpha = alpha
    }

    private fun onNewData(data: ShortContributions?) {
        if (data == null) {
            points = emptyArray()
            cellSize = 0
            invalidate()
        } else {
            val colors = data.contributionCalendar.colors
            colorPaints = arrayOf(cellPaint(0x888888, 20)) + colors.map {
                cellPaint(
                    baseColor,
                    (255 - Color.parseColor(it).luminance)
                )
            }
            points = data.contributionCalendar.weeks.map {
                it.contributionDays.map { d ->
                    colors.indexOf(d.color) + 1
                }.toIntArray()
            }.toTypedArray()
            if (points.isNotEmpty() && points[0].size < 7) {
                points[0] = IntArray(7 - points[0].size) + points[0]
            }
            requestLayout()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cellSize <= 0) {
            return
        }
        points.forEachIndexed { col, week ->
            week.forEachIndexed { row, i ->
                val left = col * cellSize.toFloat()
                val top = row * cellSize.toFloat()
                canvas.drawRect(
                    left + cellBorder,
                    top + cellBorder,
                    left + cellSize - cellBorder,
                    top + cellSize - cellBorder,
                    colorPaints[i]
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (points.isEmpty()) {
            return super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        cellSize = (parentWidth - paddingStart - paddingEnd) / points.size
        val expectedHeight = cellSize * 7 + paddingTop + paddingBottom
        setMeasuredDimension(parentWidth, expectedHeight)
    }
}