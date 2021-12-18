package com.nenpsoft.snipit.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import com.hoko.blur.HokoBlur
import com.hoko.blur.task.AsyncBlurTask
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


object BitmapTool {
    private const val MaxBitmapWidth = 3840
    private val picassoTargets = ConcurrentHashMap<Continuation<Bitmap>, Target>()

    /**
     * Suspend fun that loads bitmap from the given Uri. This function must be called from the
     * main thread.
     */
    suspend fun loadBitmap(imageUri: Uri): Bitmap {
        return suspendCoroutine {
            val target = object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom?) {
                    picassoTargets.remove(it)
                    // Make the returned bitmap mutable as we need to draw annotation shapes on it.
                    it.resumeWith(Result.success(bitmap.copy(bitmap.config, true)))
                }

                override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                    picassoTargets.remove(it)
                    it.resumeWithException(e)
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                }
            }
            picassoTargets[it] = target

            Picasso.get().load(imageUri).resize(MaxBitmapWidth, 0).onlyScaleDown().into(target)
        }
    }

    suspend fun blurBitmap(context: Context, bitmap: Bitmap, radius: Int, sample: Float): Bitmap? {
        return suspendCoroutine {
            HokoBlur.with(context)
                .scheme(HokoBlur.SCHEME_NATIVE)
                .mode(HokoBlur.MODE_BOX)
                .radius(radius)
                .sampleFactor(sample)
                .forceCopy(false)
                .needUpscale(true)
                .asyncBlur(bitmap, object : AsyncBlurTask.Callback {
                    override fun onBlurSuccess(bitmap: Bitmap) {
                        it.resumeWith(Result.success(bitmap))
                    }

                    override fun onBlurFailed(error: Throwable) {
                        it.resumeWith(Result.success(null))
                    }
                })
        }
    }
}