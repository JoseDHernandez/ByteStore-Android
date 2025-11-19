package com.example.bytestore.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

object ImageTools {
    private const val MAX_SIZE_BYTES = 30 * 1024   // 30kb
    private const val TARGET_SIZE = 750            // 750x750px

    fun adapterToWebp(
        context: Context,
        uri: Uri,
        filename: String? = null,
        partName: String = "file"
    ): MultipartBody.Part {

        // 1. Obtener Bitmap desde URI
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)

        // 2. Aplicar center crop
        val croppedBitmap = centerCrop(originalBitmap)

        // 3. Redimensionar a 750x750
        val resizedBitmap = Bitmap.createScaledBitmap(
            croppedBitmap,
            TARGET_SIZE,
            TARGET_SIZE,
            true
        )

        // 4. Convertir a WEBP y comprimir bajo 30kb
        val finalBytes = compressWebpUnder30kb(resizedBitmap)

        // 5. Crear RequestBody
        val requestBody = finalBytes.toRequestBody("image/webp".toMediaType())

        // 6. Crear multipart para Retrofit
        return MultipartBody.Part.createFormData(
            partName,
            if (!filename.isNullOrEmpty()) "${filename}.webp" else "image_${System.currentTimeMillis()}.webp",
            requestBody
        )
    }

    //Center crop para evitar deformación al convertir a 750x750
    private fun centerCrop(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val newSize = minOf(width, height)

        val xOffset = (width - newSize) / 2
        val yOffset = (height - newSize) / 2

        return Bitmap.createBitmap(bitmap, xOffset, yOffset, newSize, newSize)
    }

    //Compresión dinámica asegurando límite < 30kb
    private fun compressWebpUnder30kb(bitmap: Bitmap): ByteArray {
        var quality = 90
        var byteArray: ByteArray

        do {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, stream)
            byteArray = stream.toByteArray()
            quality -= 10
        } while (byteArray.size > MAX_SIZE_BYTES && quality > 10)

        return byteArray
    }
}