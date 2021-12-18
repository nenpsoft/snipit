package com.nenpsoft.snipit.annotation

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toRect
import com.davemorrissey.labs.subscaleview.ImageSource
import com.nenpsoft.snipit.shape.BitmapShape
import com.nenpsoft.snipit.shape.Crop
import com.nenpsoft.snipit.shape.Shape
import timber.log.Timber

class AnnotationViewOnTouchListener(
    private val fragment: AnnotationFragment,
    private val model: AnnotationViewModel,
    val density: Float
) :
    View.OnTouchListener {
    private var lastLocation: PointF? = null
    private var lastTapTime: Long = 0

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v == null || event == null) {
            return false
        }

        var scale = 1.0F
        (v as? AnnotationImageView)?.let {
            scale = it.scale
        }
        return editShapeSelected(v, event, scale)
    }

    private fun editShapeSelected(v: View, event: MotionEvent, scale: Float): Boolean {
        val pointerCount = event.pointerCount
        val action = event.actionMasked
        val hitPoint = (v as? AnnotationImageView)?.viewToSourceCoord(event.x, event.y) ?: PointF(
            event.x,
            event.y
        )
        val shape = model.shapeSelected
            ?: return if (action == MotionEvent.ACTION_DOWN && pointerCount == 1) tryHandleDoubleClick(
                v,
                hitPoint,
                null
            ) else false

        if (shape.status != Shape.Status.SELECTED) {
            return false
        }

        if (action == MotionEvent.ACTION_UP) {
            shape.editMode = Shape.EditMode.NONE
            shape.hitArea = Shape.HitArea.NONE
        }

        when (shape.editMode) {
            Shape.EditMode.MOVE -> {
                lastLocation?.let {
                    if (action == MotionEvent.ACTION_MOVE) {
                        val dx = hitPoint.x - it.x
                        val dy = hitPoint.y - it.y

                        lastLocation = PointF(hitPoint.x, hitPoint.y)
                        if (shape.offset(dx, dy)) {
                            (shape as? BitmapShape)?.let {
                                fragment.updateBitmapUnderShape(it)
                            }
                            // Triggers the shape redrawing.
                            model.shapeSelected = shape
                        }
                    }
                }
                return true
            }
            Shape.EditMode.RESIZE -> {
                if (action == MotionEvent.ACTION_MOVE) {
                    if (shape.resizeTo(hitPoint.x, hitPoint.y)) {
                        (shape as? BitmapShape)?.let {
                            fragment.updateBitmapUnderShape(it)
                        }
                        // Triggers the shape redrawing.
                        model.shapeSelected = shape
                    }
                }
                return true
            }
            Shape.EditMode.NONE -> {
                if (action == MotionEvent.ACTION_DOWN && pointerCount == 1) {
                    if (tryHandleDoubleClick(v, hitPoint, shape)) {
                        return true
                    }

                    val hitArea = shape.hitTest(PointF(hitPoint.x, hitPoint.y), density, scale)
                    shape.hitArea = hitArea
                    if (hitArea != Shape.HitArea.NONE) {
                        if (hitArea == Shape.HitArea.CENTER) {
                            shape.editMode = Shape.EditMode.MOVE
                        } else {
                            shape.editMode = Shape.EditMode.RESIZE
                        }
                    }
                    lastLocation = PointF(hitPoint.x, hitPoint.y)
                }
                if (shape.editMode != Shape.EditMode.NONE) {
                    return true
                }
            }
        }
        return false
    }

    private fun tryHandleDoubleClick(v: View, point: PointF, shape: Shape?): Boolean {
        if (lastTapTime == 0L) {
            lastTapTime = System.currentTimeMillis()
            return false
        }

        val current = System.currentTimeMillis()
        var diff = current - lastTapTime
        if (diff <= 500) {
            // It is a double click.
            lastTapTime = 0L
            return handleDoubleClick(v, point, shape)
        }

        lastTapTime = current
        return false
    }

    private fun handleDoubleClick(v: View, point: PointF, shape: Shape?): Boolean {
        val clickInShape = shape?.contains(point.x, point.y, true) ?: false
        if (clickInShape) {
            if (shape is Crop) {
                doCrop(v, shape)
            }
            // De-select the selected shape.
            model.shapeSelected = null
            return true
        } else {
            val selectedShape = model.findShapeContains(point)
            if (selectedShape != null) {
                model.shapeSelected = selectedShape
                return true
            }
            return false
        }
    }

    private fun doCrop(view: View, crop: Crop) {
        val annotationImageView = (view as? AnnotationImageView) ?: return

        val dx = crop.rect.left
        val dy = crop.rect.top
        val imageRegion = model.imageRegion
        if (imageRegion != null) {
            crop.rect.offset(imageRegion.left, imageRegion.top)
        }
        model.imageRegion = crop.rect
        model.bitmap?.let {
            try {
                annotationImageView.setImage(
                    // Must use `cachedBitmap` here. Otherwise Picasso will recycle the bitmap.
                    ImageSource.cachedBitmap(it).region(crop.rect.toRect())
                )
                model.offsetShapes(-dx, -dy)
            } catch (e: Exception) {
                // TODO: Error Handling.
                Timber.e("exception $e")
            }
        }
    }
}
