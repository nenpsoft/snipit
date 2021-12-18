package com.nenpsoft.snipit.annotation

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.nenpsoft.snipit.shape.Shape

class AnnotationImageView(context: Context, attr: AttributeSet?) :
    SubsamplingScaleImageView(context, attr) {
    var shapes: List<Shape>? = null
    var shapeSelected: Shape? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }

        canvas.save()
        val translate = viewToSourceCoord(PointF(0F, 0F))
        translate?.let {
            canvas.scale(scale, scale)
            canvas.translate(-translate.x, -translate.y)
            visibleSource()?.let {
                canvas.clipRect(it)
            }
            shapes?.forEach {
                it.draw(context, canvas, resources.displayMetrics.density, scale)
            }
            shapeSelected?.draw(context, canvas, resources.displayMetrics.density, scale)
        }
        canvas.restore()
    }

    // Returns the visible source image area.
    fun visibleSource(): RectF? {
        if (center == null) {
            return null
        }

        val topLeft = viewToSourceCoord(PointF(0F, 0F))
        val bottomRight = viewToSourceCoord(PointF(width.toFloat(), height.toFloat()))

        if (topLeft == null || bottomRight == null) {
            return null
        }

        bottomRight.offset(minOf(0F, topLeft.x), minOf(0F, topLeft.y))

        topLeft.x = maxOf(0F, topLeft.x)
        topLeft.y = maxOf(0F, topLeft.y)

        return RectF(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y)
    }
}

