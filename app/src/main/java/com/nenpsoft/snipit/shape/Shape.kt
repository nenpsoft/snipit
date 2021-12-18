package com.nenpsoft.snipit.shape

import android.content.Context
import android.graphics.*
import com.nenpsoft.snipit.common.shrink

const val StrokeWidthDP = 3.0F
private const val BullEyeBackgroundSizeDP = 16F
private const val BullEyeForegroundSizeDP = 12F
private const val HotspotSizeDP = 40F

private val EdgeBlue = Color.parseColor("#448aff")
private val CenterRed = Color.parseColor("#ef5350")

interface Shape {
    enum class HitArea {
        NONE, TOP_LEFT, TOP, TOP_RIGHT, RIGHT, BOTTOM_RIGHT, BOTTOM, BOTTOM_LEFT, LEFT, CENTER
    }

    enum class EditMode {
        NONE, MOVE, RESIZE
    }

    enum class Status {
        NORMAL, SELECTED
    }

    // The vertices of the shape's enclosing rectangle. The order of the vertices is top-left,
    // top-right, bottom-right and bottom-left.
    var rect: RectF
    var color: Int
    var hitArea: HitArea
    var editMode: EditMode
    var status: Status

    // Width of paint stroke in DP.
    val strokeWidth: Float

    // Extra properties when defining a shape.
    val properties: Map<String, Any>

    val normalizedRect: RectF
        get() {
            val r = RectF()
            r.top = minOf(rect.top, rect.bottom)
            r.left = minOf(rect.left, rect.right)
            r.bottom = maxOf(rect.top, rect.bottom)
            r.right = maxOf(rect.left, rect.right)
            return r
        }

    // The 8 points on the enclosing box for a user to interact with the shape.
    val disabledHitArea: Set<HitArea>
        get() = setOf()
    val hitBox: List<Pair<PointF, HitArea>>
        get() = listOf(
            PointF(rect.left, rect.top) to HitArea.TOP_LEFT,
            PointF(rect.centerX(), rect.top) to HitArea.TOP,
            PointF(rect.right, rect.top) to HitArea.TOP_RIGHT,
            PointF(rect.right, rect.centerY()) to HitArea.RIGHT,
            PointF(rect.right, rect.bottom) to HitArea.BOTTOM_RIGHT,
            PointF(rect.centerX(), rect.bottom) to HitArea.BOTTOM,
            PointF(rect.left, rect.bottom) to HitArea.BOTTOM_LEFT,
            PointF(rect.left, rect.centerY()) to HitArea.LEFT,
            PointF(rect.centerX(), rect.centerY()) to HitArea.CENTER,
        ).filterNot {
            it.second in disabledHitArea
        }

    fun draw(context: Context, canvas: Canvas, density: Float, scale: Float)

    fun hitTest(point: PointF, density: Float, scale: Float): HitArea {
        // Fine hit test.
        hitBox.forEach {
            val r2 =
                BullEyeForegroundSizeDP / 2 * BullEyeForegroundSizeDP / 2 * density * density / scale / scale
            val dis =
                (point.x - it.first.x) * (point.x - it.first.x) + (point.y - it.first.y) * (point.y - it.first.y)
            if (dis <= r2) {
                return it.second
            }
        }

        // Coarse hit test.
        val hotspotSize = HotspotSizeDP * density / scale
        hitBox.forEach {
            val rect = rectangleWithCenter(it.first, hotspotSize, hotspotSize)
            if (rect.contains(point.x, point.y)) {
                return it.second
            }
        }
        return HitArea.NONE
    }

    // Move the shape.
    // Return true if shape is moved; otherwise false.
    fun offset(dx: Float, dy: Float): Boolean {
        if (editMode != EditMode.MOVE) {
            return false
        }
        rect.offset(dx, dy)
        return true
    }

    // Resize the shape.
    // Return true if shape is resize; otherwise false.
    fun resizeTo(x: Float, y: Float, density: Float = 1.0F): Boolean {
        if (editMode != EditMode.RESIZE) {
            return false
        }

        when (hitArea) {
            HitArea.NONE -> return false
            HitArea.TOP_LEFT -> {
                rect.top = y
                rect.left = x
            }
            HitArea.TOP -> {
                rect.top = y
            }
            HitArea.TOP_RIGHT -> {
                rect.top = y
                rect.right = x
            }
            HitArea.RIGHT -> {
                rect.right = x
            }
            HitArea.BOTTOM_RIGHT -> {
                rect.bottom = y
                rect.right = x
            }
            HitArea.BOTTOM -> {
                rect.bottom = y
            }
            HitArea.BOTTOM_LEFT -> {
                rect.bottom = y
                rect.left = x
            }
            HitArea.LEFT -> {
                rect.left = x
            }
            HitArea.CENTER -> {
            }
        }

        return true
    }

    fun contains(x: Float, y: Float, relax: Boolean = false): Boolean {
        var left = minOf(rect.left, rect.right)
        var right = maxOf(rect.left, rect.right)
        var top = minOf(rect.top, rect.bottom)
        var bottom = maxOf(rect.top, rect.bottom)

        val rect = RectF(left, top, right, bottom)

        // The shape is too narrow, relaxing the test.
        val rv = 128F
        if (relax) {
            var dx = if (right - left < rv) rv / 2F else 0F
            var dy = if (bottom - top < rv) rv / 2F else 0F
            rect.shrink(-dx, -dy)
        }

        return rect.contains(x, y)
    }
}

interface AnnotationShape : Shape {
    fun drawHitBox(context: Context, canvas: Canvas, density: Float, scale: Float) {
        hitBox.reversed().forEach { p ->
            if (p.second == Shape.HitArea.CENTER) {
                drawBullEye(
                    context,
                    canvas,
                    density,
                    scale,
                    p.first,
                    foreground = CenterRed,
                    alpha = 255
                )
            } else {
                drawBullEye(context, canvas, density, scale, p.first)
            }
        }
    }

    fun drawBullEye(
        context: Context,
        canvas: Canvas,
        density: Float,
        scale: Float,
        location: PointF,
        background: Int = Color.WHITE,
        foreground: Int = EdgeBlue,
        backgroundSize: Float = BullEyeBackgroundSizeDP * density / scale,
        foregroundSize: Float = BullEyeForegroundSizeDP * density / scale,
        alpha: Int = 175
    ) {
        val backgroundPaint = Paint()
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.color = background
        backgroundPaint.alpha = alpha
        canvas.drawArc(
            rectangleWithCenter(location, backgroundSize, backgroundSize),
            0F,
            360F,
            false,
            backgroundPaint
        )

        val foregroundPaint = Paint()
        foregroundPaint.style = Paint.Style.FILL
        foregroundPaint.color = foreground
        foregroundPaint.alpha = alpha
        canvas.drawArc(
            rectangleWithCenter(location, foregroundSize, foregroundSize),
            0F,
            360F,
            false,
            foregroundPaint
        )
    }
}

fun rectangleWithCenter(center: PointF, width: Float, height: Float): RectF {
    val halfW = width / 2
    val halfH = height / 2
    return RectF(center.x - halfW, center.y - halfH, center.x + halfW, center.y + halfH)
}