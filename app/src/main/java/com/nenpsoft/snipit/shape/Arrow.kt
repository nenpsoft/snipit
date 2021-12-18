package com.nenpsoft.snipit.shape

import android.content.Context
import android.graphics.*

private val DisabledHitArea = setOf(
    Shape.HitArea.TOP,
    Shape.HitArea.BOTTOM,
    Shape.HitArea.LEFT,
    Shape.HitArea.RIGHT,
    Shape.HitArea.TOP_RIGHT,
    Shape.HitArea.BOTTOM_LEFT
)

class Arrow(
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

    override fun draw(context: Context, canvas: Canvas, density: Float, scale: Float) {
        val path = Path()
        val sp = PointF(rect.left, rect.top)
        val ep = PointF(rect.right, rect.bottom)

        path.moveTo(sp.x, sp.y)
        path.lineTo(ep.x, ep.y)

        val matrix = Matrix()
        matrix.setScale(0.1F, 0.1F, sp.x, sp.y)
        matrix.postRotate(30.0F, sp.x, sp.y)

        var path1 = Path()
        path.transform(matrix, path1)

        matrix.setScale(0.1F, 0.1F, sp.x, sp.y)
        matrix.postRotate(-30.0F, sp.x, sp.y)

        var path2 = Path()
        path.transform(matrix, path2)

        path.addPath(path1)
        path.addPath(path2)

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = strokeWidth * density
        paint.strokeCap = Paint.Cap.ROUND

        canvas.drawPath(path, paint)
        if (status == Shape.Status.SELECTED) {
            drawHitBox(context, canvas, density, scale)
        }
    }
}