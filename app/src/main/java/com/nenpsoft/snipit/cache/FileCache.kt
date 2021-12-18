package com.nenpsoft.snipit.cache

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.MessageDigest

object FileCache {
    fun save(context: Context, imageUrl: Uri, bitmap: Bitmap): String {
        val fileName = "${fileName(context, imageUrl)}.jpg"
        FileOutputStream(File(fileName)).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, it)
        }
        return fileName
    }

    private fun fileName(context: Context, imageUrl: Uri): String {
        val cachePath = File(context.cacheDir, "images")
        if (!cachePath.exists()) {
            cachePath.mkdirs()
        }
        return "${context.cacheDir}/images/${md5(imageUrl.toString())}"
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }
}