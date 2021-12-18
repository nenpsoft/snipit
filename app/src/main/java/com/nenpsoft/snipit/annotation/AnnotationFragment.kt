package com.nenpsoft.snipit.annotation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.PopupWindow
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.graphics.toRect
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.davemorrissey.labs.subscaleview.ImageSource
import com.nenpsoft.snipit.R
import com.nenpsoft.snipit.cache.BitmapTool
import com.nenpsoft.snipit.cache.FileCache
import com.nenpsoft.snipit.common.loadDefaultColor
import com.nenpsoft.snipit.common.saveDefaultColor
import com.nenpsoft.snipit.common.shrink
import com.nenpsoft.snipit.databinding.FragmentAnnotationBinding
import com.nenpsoft.snipit.shape.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File


const val ArgumentUri = "image_uri"

private const val DefaultShapeWidthDP = 128
private const val DefaultShapeHeightDP = 76
private const val BlurRadius = 20
private const val BlurSampling = 30F

private enum class ShapeButton(val id: Int) {
    Arrow(R.id.arrow_button),
    Ellipse(R.id.ellipse_button),
    Rectangle(R.id.rectangle_button),
    Text(R.id.text_button),
    Bitmap(R.id.blurring_button)
}

class AnnotationFragment : Fragment(), ColorChangeListener, TextChangeListener {
    private lateinit var binding: FragmentAnnotationBinding

    // Shapes popup menu.
    private var shapesMenu: PopupWindow? = null

    // Activity view model.
    private val model: AnnotationViewModel by activityViewModels()

    private lateinit var openAlbum: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        openAlbum = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            it.data?.data?.let {
                reloadBitmap(it)
            }
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Colors.current = Colors.valueOf(loadDefaultColor(requireContext()))

        binding = DataBindingUtil.inflate<FragmentAnnotationBinding>(
            inflater, R.layout.fragment_annotation, container, false
        )

        binding.splashScreen.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
        }
        binding.splashScreen.loadUrl("file:///android_asset/snipit.html")

        binding.annotationImageView.apply {
            shapeSelected = model.shapeSelected
            shapes = model.annotationShapes
        }

        binding.annotationImageView.setOnTouchListener(
            AnnotationViewOnTouchListener(
                this,
                model,
                resources.displayMetrics.density
            )
        )

        setButtonOnClickListeners()
        updateColorButton(Colors.current.value)

        setModelObservers()
        return binding.root
    }

    override fun onStart() {
        (arguments?.get(ArgumentUri) as? Uri)?.let {
            arguments?.remove(ArgumentUri)
            reloadBitmap(it)
        }
        super.onStart()
    }

    override fun onStop() {
        saveDefaultColor(Colors.current.value, requireContext())
        super.onStop()
    }

    private fun reloadBitmap(uri: Uri) {
        lifecycleScope.launch {
            val progressDialog = ProgressDialog()
            progressDialog.show(requireActivity().supportFragmentManager, "progressDialog")
            model.loadBitmap(uri)
            progressDialog.dismiss()
        }
    }

    private fun addShape(id: Int) {
        val center = binding.annotationImageView.center ?: return
        var newShape: AnnotationShape? = null
        val density = resources.displayMetrics.density
        val scale = binding.annotationImageView.scale
        val rect = rectangleWithCenter(
            center,
            density * DefaultShapeWidthDP / scale,
            density * DefaultShapeHeightDP / scale
        )

        when (id) {
            ShapeButton.Arrow.id -> {
                newShape = Arrow(rect).apply {
                    status = Shape.Status.SELECTED
                }
            }
            ShapeButton.Ellipse.id -> {
                newShape = Ellipse(rect).apply {
                    status = Shape.Status.SELECTED
                }
            }
            ShapeButton.Rectangle.id -> {
                newShape = Rectangle(rect).apply {
                    status = Shape.Status.SELECTED
                }
            }
            ShapeButton.Text.id -> {
                newShape = Text(rect).apply {
                    status = Shape.Status.SELECTED
                }
            }
            ShapeButton.Bitmap.id -> {
                val boundingRect = RectF(
                    0F,
                    0F,
                    binding.annotationImageView.sWidth.toFloat(),
                    binding.annotationImageView.sHeight.toFloat()
                )
                newShape = BitmapShape(boundingRect, rect).apply {
                    status = Shape.Status.SELECTED
                }
                updateBitmapUnderShape(newShape)
            }
        }
        newShape?.color = Colors.current.value
        model.shapeSelected = newShape
    }

    private fun showShapesPopupMenu(view: View) {
        val layoutInflater =
            context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

        layoutInflater?.inflate(R.layout.annotaion_shapes_menu, null)?.let { popupView ->
            val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            popupView.measure(measureSpec, measureSpec)
            shapesMenu = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                isOutsideTouchable = true
                elevation = 10F
            }

            ShapeButton.values().forEach { shapeButton ->
                popupView.findViewById<ImageButton>(shapeButton.id)?.setOnClickListener { _ ->
                    addShape(shapeButton.id)
                }
            }

            shapesMenu?.showAsDropDown(view, 0, -popupView.measuredHeight)
        }
    }

    private fun showCropRect() {
        if (model.shapeSelected is Crop) {
            model.shapeSelected = null
            return
        }

        val cropRect = binding.annotationImageView.visibleSource() ?: return
        val rect = RectF(
            0F,
            0F,
            binding.annotationImageView.sWidth.toFloat(),
            binding.annotationImageView.sHeight.toFloat()
        )

        cropRect.shrink(
            48 * resources.displayMetrics.density / binding.annotationImageView.scale,
            48 * resources.displayMetrics.density / binding.annotationImageView.scale
        )

        val shape = Crop(rect, cropRect)
        model.shapeSelected = shape
    }

    private fun setModelObservers() {
        val shapeSelectedObserver = Observer<Shape?> { shape ->
            binding.buttons.deleteButton.isEnabled = shape is AnnotationShape
            binding.annotationImageView.shapeSelected = shape
            if (shape is AnnotationShape) {
                updateColorButton(shape.color)
            } else {
                updateColorButton(Colors.current.value)
            }
            binding.buttons.inputButton.isEnabled = shape is Text
            binding.annotationImageView.invalidate()
        }

        val shapesObserver = Observer<List<AnnotationShape>?> {
            binding.annotationImageView.shapes = it
            binding.annotationImageView.invalidate()
        }

        val bitmapObserver = Observer<Bitmap?> {
            // Must use `cachedBitmap` here. Otherwise Picasso will recycle the bitmap.
            binding.annotationImageView.setImage(ImageSource.cachedBitmap(it))
            binding.splashScreen.isVisible = false
            binding.buttons.cropButton.isEnabled = it != null
            binding.buttons.addButton.isEnabled = it != null
            binding.buttons.shareButton.isEnabled = it != null
        }

        model.setObserves(requireActivity(), shapeSelectedObserver, shapesObserver, bitmapObserver)
    }

    private fun updateColorButton(color: Int) {
        binding.buttons.colorButton.setColorFilter(color)
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        openAlbum.launch(intent)
    }

    private fun setButtonOnClickListeners() {
        binding.buttons.galleryButton.setOnClickListener {
            selectPhoto()
        }

        binding.buttons.addButton.setOnClickListener {
            model.shapeSelected = null
            showShapesPopupMenu(it)
        }
        binding.buttons.addButton.isEnabled = false

        binding.buttons.deleteButton.isEnabled = false
        binding.buttons.deleteButton.setOnClickListener {
            deleteSelectedShape()
        }

        binding.buttons.cropButton.setOnClickListener {
            showCropRect()
        }
        binding.buttons.cropButton.isEnabled = false

        binding.buttons.colorButton.setOnClickListener {
            ColorListDialogFragment(this).show(
                requireActivity().supportFragmentManager,
                "colorDialog"
            )
        }

        binding.buttons.shareButton.isEnabled = false
        binding.buttons.shareButton.setOnClickListener {
            share()
        }

        binding.buttons.inputButton.isEnabled = false
        binding.buttons.inputButton.setOnClickListener {
            (model.shapeSelected as? Text)?.let {
                TextInputDialogFragment(this, it.text).show(
                    requireActivity().supportFragmentManager,
                    "textInputDialog"
                )
            }
        }
    }

    private fun share() {
        lifecycleScope.launch {
            model.bitmap?.let {
                val progressDialog = ProgressDialog()
                val crop = model.imageRegion?.toRect() ?: Rect(0, 0, it.width, it.height)
                val bitmap =
                    Bitmap.createBitmap(it, crop.left, crop.top, crop.width(), crop.height())
                val canvas = Canvas(bitmap)
                model.shapeSelected = null
                model.annotationShapes.forEach { shape ->
                    shape.draw(requireContext(), canvas, resources.displayMetrics.density, 1.0F)
                }
                progressDialog.show(requireActivity().supportFragmentManager, "progressDialog")
                val job = async(Dispatchers.IO) {
                    FileCache.save(requireContext(), model.imageUri ?: Uri.EMPTY, bitmap)
                }
                val filename = job.await()
                progressDialog.dismiss()
                FileProvider.getUriForFile(
                    requireContext(),
                    "com.nenpsoft.snipit.fileprovider",
                    File(filename)
                )?.let { imageUri ->
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    shareIntent.setDataAndType(
                        imageUri,
                        requireActivity().contentResolver.getType(imageUri)
                    )
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                    startActivity(Intent.createChooser(shareIntent, "Choose an app"))
                }
            }
        }
    }

    fun updateBitmapUnderShape(shape: Shape) {
        (shape as? BitmapShape)?.let { bitmapShape ->
            lifecycleScope.launch {
                model.bitmap?.let { bitmap ->
                    val offset = model.imageRegion?.toRect() ?: Rect(0, 0, 0, 0)
                    val r = shape.normalizedRect.toRect()
                    r.offset(offset.left, offset.top)
                    BitmapTool.blurBitmap(
                        requireContext(),
                        Bitmap.createBitmap(
                            bitmap,
                            maxOf(r.left, 0),
                            maxOf(r.top, 0),
                            minOf(bitmap.width, r.width()),
                            minOf(bitmap.height, r.height())
                        ),
                        BlurRadius,
                        BlurSampling
                    )?.let { blurredBitmap ->
                        bitmapShape.bitmap = blurredBitmap
                    }
                    binding.annotationImageView.invalidate()
                }
            }
        }
    }

    private fun deleteSelectedShape() {
        model.shapeSelected?.let {
            if (model.shapeSelected !is AnnotationShape) {
                return
            } else {
                model.deleteShape(it)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.data?.let {
            reloadBitmap(it)
        }
    }

    override fun colorChanged(newColor: Color) {
        val shapeSelected = model.shapeSelected as? AnnotationShape
        if (shapeSelected != null) {
            shapeSelected.color = newColor.value
            binding.annotationImageView.invalidate()
        } else {
            Colors.current = newColor
        }
        updateColorButton(newColor.value)
    }

    override fun textChanged(text: String) {
        val textShape = model.shapeSelected as? Text ?: return
        textShape.text = text
        binding.annotationImageView.invalidate()
    }
}