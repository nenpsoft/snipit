package com.nenpsoft.snipit.shape

import android.content.Context
import android.graphics.*
import kotlin.math.abs

const val TextProperty = "text"

// Default font size in DP.
private const val DefaultFontSize = 20F
private const val MinRectWidth = 64F
private const val MinRectHeight = 24F

// Text padding in DP.
private const val TextPaddingHorizontal = 12.0F
private const val TextPaddingVertical = 8.0F
private val DisabledHitArea =
    setOf(Shape.HitArea.TOP, Shape.HitArea.BOTTOM, Shape.HitArea.LEFT, Shape.HitArea.RIGHT)

class Text(
    override var rect: RectF,
    override val properties: Map<String, Any> = mapOf(),
    override var color: Int = Color.BLACK,
    override val strokeWidth: Float = StrokeWidthDP,
) : AnnotationShape {
    override var status = Shape.Status.NORMAL
    override var hitArea = Shape.HitArea.NONE
    override var editMode = Shape.EditMode.NONE
    override val disabledHitArea: Set<Shape.HitArea>
        get() = DisabledHitArea

    var text = (properties[TextProperty] as? String) ?: "text"

    override fun draw(context: Context, canvas: Canvas, density: Float, scale: Float) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = color
        paint.strokeWidth = 0F
        paint.textSize = calculateFontSize(density)

        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val bottomOffset = (rect.height() - textBounds.height()) / 2 + TextPaddingVertical
        canvas.drawText(
            text,
            rect.left + (rect.width() - textBounds.width()) / 2,
            rect.bottom - bottomOffset,
            paint
        )

        if (status == Shape.Status.SELECTED) {
            drawHitBox(context, canvas, density, scale)
        }
    }

    private fun calculateFontSize(density: Float): Float {
        var paint = Paint()
        val baseTextSize = 1_000_000F
        paint.textSize = baseTextSize
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val sizeW: Float =
            (abs(rect.width()) - TextPaddingHorizontal * 2 * density) * baseTextSize / textBounds.width()
                .toFloat()
        val sizeH: Float =
            abs(rect.height() - TextPaddingVertical * 2 * density) * baseTextSize / textBounds.height()
                .toFloat()
        return if (sizeW < sizeH) sizeW else sizeH
    }
}
