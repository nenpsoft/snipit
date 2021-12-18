package com.nenpsoft.snipit.shape

import android.content.Context
import android.graphics.*

private const val HitboxHandleSizeDP = 12F
private const val MinCropSizeDP = 96F
private const val HitboxStrokeWidthDP = 2F

class Crop(
    override val boundingRect: RectF,
    override var rect: RectF,
    override val properties: Map<String, Any> = mapOf(),
    override var color: Int = Color.BLACK,
    override val strokeWidth: Float = StrokeWidthDP,
) : BoundedShape {
    override var status = Shape.Status.SELECTED
    override var hitArea: Shape.HitArea = Shape.HitArea.NONE
    override var editMode: Shape.EditMode = Shape.EditMode.NONE
    override var minSizeInDP = MinCropSizeDP

    override fun draw(context: Context, canvas: Canvas, density: Float, scale: Float) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK

        val path = Path()
        path.addRect(boundingRect, Path.Direction.CW)

        // Set the alpha of the paint. Must call restore later.
        canvas.saveLayerAlpha(boundingRect, 200)

        val cropPath = Path()
        cropPath.addRect(rect, Path.Direction.CCW)

        path.addPath(cropPath)
        path.fillType = Path.FillType.EVEN_ODD
        canvas.drawPath(path, paint)
        canvas.restore()

        drawHitBox(canvas, density, scale)
    }

    private fun drawHitBox(canvas: Canvas, density: Float, scale: Float) {
        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = HitboxStrokeWidthDP * density / scale

        val path = Path()
        path.addRect(rect, Path.Direction.CCW)

        canvas.drawPath(path, paint)
        drawHandle(canvas, density, scale)
    }

    private fun drawHandle(
        canvas: Canvas,
        density: Float,
        scale: Float,
    ) {
        val rectSize = HitboxHandleSizeDP * density / scale
        val path = Path()

        hitBox.reversed().forEach { p ->
            path.addRect(rectangleWithCenter(p.first, rectSize, rectSize), Path.Direction.CCW)
        }

        val paint = Paint()
        paint.color = Color.LTGRAY
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)
    }
}

