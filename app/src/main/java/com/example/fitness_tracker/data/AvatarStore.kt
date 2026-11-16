package com.example.fitness_tracker.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.File
import kotlin.math.roundToInt

// class for avatar photo storage,
// copies only the chosen image into private storage
internal class AvatarStore(private val context: Context) {
    fun importPhoto(uri: Uri): String {
        val image = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            val scale = minOf(1f, 512f / maxOf(info.size.width, info.size.height))
            decoder.setTargetSize((info.size.width * scale).roundToInt().coerceAtLeast(1), (info.size.height * scale).roundToInt().coerceAtLeast(1))
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
        val file = File(context.filesDir, "avatar-${System.nanoTime()}.png")
        try {
            file.outputStream().use { check(image.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        } catch (error: Exception) { file.delete(); throw error }
        finally { image.recycle() }
        return file.absolutePath
    }
}
