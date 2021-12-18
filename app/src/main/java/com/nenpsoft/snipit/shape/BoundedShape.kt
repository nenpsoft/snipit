package com.nenpsoft.snipit.shape

import android.graphics.RectF

interface BoundedShape : Shape {
    val boundingRect: RectF
    var minSizeInDP: Float

    // Move the shape.
    // Return true if shape is moved; otherwise false.
    override fun offset(dx: Float, dy: Float): Boolean {
        if (editMode != Shape.EditMode.MOVE) {
            return false
        }

        val minDx = boundingRect.left - rect.left
        val maxDx = boundingRect.right - rect.right
        val minDy = boundingRect.top - rect.top
        val maxDy = boundingRect.bottom - rect.bottom
        rect.offset(dx.coerceIn(minDx, maxDx), dy.coerceIn(minDy, maxDy))
        return true
    }

    // Move the bounding rect.
    fun offsetBoundingRect(dx: Float, dy: Float) {
        boundingRect.offset(dx, dy)
    }

    // Resize the shape.
    // Return true if shape is resize; otherwise false.
    override fun resizeTo(x: Float, y: Float, density: Float): Boolean {
        if (editMode != Shape.EditMode.RESIZE) {
            return false
        }

        val minSize = minSizeInDP * density

        val maxTop = maxOf(0F, rect.bottom - minSize)
        val maxLeft = maxOf(0F, rect.right - minSize)
        val minRight = minOf(rect.left + minSize, boundingRect.right)
        val minBottom = minOf(rect.top + minSize, boundingRect.bottom)

        when (hitArea) {
            Shape.HitArea.NONE -> return false
            Shape.HitArea.TOP_LEFT -> {
                rect.top = y.coerceIn(0F, maxTop)
                rect.left = x.coerceIn(0F, maxLeft)
            }
            Shape.HitArea.TOP -> {
                rect.top = y.coerceIn(0F, maxTop)
            }
            Shape.HitArea.TOP_RIGHT -> {
                rect.top = y.coerceIn(0F, maxTop)
                rect.right = x.coerceIn(minRight, boundingRect.right)
            }
            Shape.HitArea.RIGHT -> {
                rect.right = x.coerceIn(minRight, boundingRect.right)
            }
            Shape.HitArea.BOTTOM_RIGHT -> {
                rect.bottom = y.coerceIn(minBottom, boundingRect.bottom)
                rect.right = x.coerceIn(minRight, boundingRect.right)
            }
            Shape.HitArea.BOTTOM -> {
                rect.bottom = y.coerceIn(minBottom, boundingRect.bottom)
            }
            Shape.HitArea.BOTTOM_LEFT -> {
                rect.bottom = y.coerceIn(minBottom, boundingRect.bottom)
                rect.left = x.coerceIn(0F, maxLeft)
            }
            Shape.HitArea.LEFT -> {
                rect.left = x.coerceIn(0F, maxLeft)
            }
            Shape.HitArea.CENTER -> {
            }
        }

        return true
    }
}