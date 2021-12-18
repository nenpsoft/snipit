package com.nenpsoft.snipit.shape

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

private const val MinSizeInDP = 4F

class BitmapShape(
    override val boundingRect: RectF,
    override var rect: RectF,
    override val properties: Map<String, Any> = mapOf(),
    override var color: Int = Color.BLACK,
    override val strokeWidth: Float = StrokeWidthDP
) : AnnotationShape, BoundedShape {
    override var status = Shape.Status.NORMAL
    override var hitArea = Shape.HitArea.NONE
    override var editMode = Shape.EditMode.NONE
    override var minSizeInDP = MinSizeInDP

    var bitmap: Bitmap? = null

    override fun draw(context: Context, canvas: Canvas, density: Float, scale: Float) {
        bitmap?.let {
            canvas.drawBitmap(it, null, rect, null)
        }

        if (status == Shape.Status.SELECTED) {
            drawHitBox(context, canvas, density, scale)
        }
    }
}