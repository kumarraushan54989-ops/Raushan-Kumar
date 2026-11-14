package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

suspend fun encodePickedImageToBase64(
  context: Context,
  uri: Uri,
  maxDimensionPx: Int = 480,
  maxBase64Length: Int = 180_000
): String? = withContext(Dispatchers.IO) {
  runCatching {
    val resolver = context.contentResolver

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, bounds)
    }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

    var sampleSize = 1
    var largest = max(bounds.outWidth, bounds.outHeight)
    while (largest / sampleSize > maxDimensionPx * 2) {
      sampleSize *= 2
    }

    val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val decoded = resolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, decodeOpts)
    } ?: return@runCatching null

    val currentMax = max(decoded.width, decoded.height)
    val scaled = if (currentMax > maxDimensionPx) {
      val ratio = maxDimensionPx.toFloat() / currentMax.toFloat()
      val targetW = (decoded.width * ratio).toInt().coerceAtLeast(1)
      val targetH = (decoded.height * ratio).toInt().coerceAtLeast(1)
      Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
        if (it !== decoded) decoded.recycle()
      }
    } else {
      decoded
    }

    var result: String? = null
    for (quality in intArrayOf(78, 62, 46, 32)) {
      val out = ByteArrayOutputStream()
      scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
      val encoded = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
      if (encoded.length <= maxBase64Length) {
        result = encoded
        break
      }
    }
    scaled.recycle()
    result
  }.getOrNull()
}

fun decodeBase64ToImageBitmap(base64: String): ImageBitmap? {
  if (base64.isBlank()) return null
  return runCatching {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
  }.getOrNull()
}
