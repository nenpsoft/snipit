package com.nenpsoft.snipit.annotation

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.nenpsoft.snipit.cache.BitmapTool
import com.nenpsoft.snipit.shape.AnnotationShape
import com.nenpsoft.snipit.shape.BoundedShape
import com.nenpsoft.snipit.shape.Shape

class AnnotationViewModel : ViewModel() {
    private var _imageUri: Uri? = null
    private val _bitmap = MutableLiveData<Bitmap?>()

    // Region of the original image that is visible after cropping.
    var imageRegion: RectF? = null
    private val _annotationShapes = MutableLiveData(mutableListOf<AnnotationShape>())
    private val _shapeSelected = MutableLiveData<Shape?>()

    val imageUri: Uri?
        get() = _imageUri

    val bitmap: Bitmap?
        get() = _bitmap.value

    val annotationShapes: List<AnnotationShape>
        get() = _annotationShapes.value?.toList() ?: listOf()

    var shapeSelected: Shape?
        get() = _shapeSelected.value
        set(value) {
            (_shapeSelected.value as? AnnotationShape)?.let {
                it.status = Shape.Status.NORMAL
            }
            (value as? AnnotationShape)?.let {
                val shapes = _annotationShapes.value
                if (shapes?.contains(it) != true) {
                    shapes?.add(it)
                }
                _annotationShapes.value = shapes
            }
            _shapeSelected.value = value
            value?.status = Shape.Status.SELECTED
        }

    fun findShapeContains(point: PointF): Shape? {
        _annotationShapes.value?.reversed()?.forEach {
            if (it.contains(point.x, point.y)) {
                return it
            }
        }

        _annotationShapes.value?.reversed()?.forEach {
            if (it.contains(point.x, point.y, true)) {
                return it
            }
        }

        return null
    }

    fun offsetShapes(dx: Float, dy: Float) {
        _annotationShapes.value?.forEach {
            val editMode = it.editMode
            it.editMode = Shape.EditMode.MOVE
            (it as? BoundedShape)?.let { boundedShape ->
                boundedShape.offsetBoundingRect(dx, dy)
            }
            it.offset(dx, dy)
            it.editMode = editMode
        }
    }

    fun setObserves(
        owner: LifecycleOwner,
        selectedShapeObserver: Observer<Shape?>? = null,
        shapesObserver: Observer<List<AnnotationShape>?>? = null,
        bitmapObserver: Observer<Bitmap?>? = null
    ) {
        selectedShapeObserver?.let {
            _shapeSelected.observe(owner, selectedShapeObserver)
        }

        shapesObserver?.let {
            _annotationShapes.observe(owner, shapesObserver)
        }

        bitmapObserver?.let {
            _bitmap.observe(owner, bitmapObserver)
        }
    }

    suspend fun loadBitmap(uri: Uri) {
        // TODO: handle exception
        _bitmap.value = BitmapTool.loadBitmap(uri)
        imageRegion = null
        _imageUri = uri
        _shapeSelected.value = null
        _annotationShapes.value = mutableListOf()
    }

    fun deleteShape(shape: Shape?) {
        _annotationShapes.value?.let {
            it.remove(shape)
            _annotationShapes.value = it
        }

        if (shapeSelected == shape) {
            shapeSelected = null
        }
    }
}