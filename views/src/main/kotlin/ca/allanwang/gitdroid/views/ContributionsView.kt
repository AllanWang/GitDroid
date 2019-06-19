package ca.allanwang.gitdroid.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.databinding.BindingAdapter
import github.fragment.ShortContributions

@BindingAdapter("contributions")
fun ContributionsView.contributions(data: ShortContributions?) {
    contributions = data
}

private class TitleEntry(val text: String, val loc: PointF)

class ContributionsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        setWillNotDraw(false)
    }

    private val textPaint = TextPaint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    var contributions: ShortContributions? = null
        set(value) {
            field = value
            onNewData(value)
        }
    private var points: Array<IntArray> = emptyArray()
    private var colorPaints: Array<Paint> = (0..levels.size).map { i ->
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.MAGENTA
            alpha = (200 / (levels.size + 1) * i) + 55
        }
    }.toTypedArray()
    private var labels: Array<TitleEntry> = emptyArray()
    private var cellSize: Int = 0
    private var labelHeight: Int = 0

    private companion object {
        // If less than el at i, then level i
        // Max level i + 1
        private val levels = intArrayOf(1, 5, 11, 18)
    }

    private fun onNewData(data: ShortContributions?) {
        if (data == null) {
            points = emptyArray()
            colorPaints = emptyArray()
            labels = emptyArray()
            cellSize = 0
            labelHeight = 0
            invalidate()
        } else {
            colorPaints = data.contributionCalendar.colors.map { textPaint(it) }.toTypedArray()
            points = data.contributionCalendar.weeks.map {
                it.contributionDays.map { d ->
                    levels.firstOrNull { max -> d.contributionCount < max } ?: levels.size
                }.toIntArray()
            }.toTypedArray()
            if (points.isNotEmpty() && points[0].size < 7) {
                points[0] = IntArray(7 - points[0].size) + points[0]
            }
            requestLayout()
        }
    }

    private fun textPaint(color: String): Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        setColor(Color.parseColor(color))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cellSize <= 0) {
            return
        }
        points.forEachIndexed { col, week ->
            week.forEachIndexed { row, i ->
                val left = col * cellSize.toFloat()
                val top = labelHeight + row * cellSize.toFloat()
                canvas.drawRect(
                    left,
                    top,
                    left + cellSize,
                    top + cellSize,
                    colorPaints[i]
                )
            }
        }
        labels.forEach {
            canvas.drawText(it.text, it.loc.x, it.loc.y, textPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val data = contributions ?: return super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        cellSize = (parentWidth) / points.size
        labelHeight = cellSize * 2 // TODO add max cap for title? Currently height of 2
        // TODO compute labels
        val expectedHeight = cellSize * 7 + labelHeight
        textPaint.textSize = cellSize * 2f
        setMeasuredDimension(parentWidth, expectedHeight)
    }
}