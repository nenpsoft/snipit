package com.nenpsoft.snipit.shape

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class Rectangle(
    override var rect: RectF,
    override val properties: Map<String, Any> = mapOf(),
    override var color: Int = Color.BLACK,
    override val strokeWidth: Float = StrokeWidthDP,
) : AnnotationShape {
    override var status = Shape.Status.NORMAL
    override var hitArea = Shape.HitArea.NONE
    override var editMode = Shape.EditMode.NONE

    override fun draw(context: Context, canvas: Canvas, density: Float, scale: Float) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = strokeWidth * density
        canvas.drawRect(rect, paint)
        if (status == Shape.Status.SELECTED) {
            drawHitBox(context, canvas, density, scale)
        }
    }
}