package com.example.pocketmanage.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ProfileAvatarStorage {
    private const val DIR = "avatars"
    private const val FILE_NAME = "profile.jpg"

    fun avatarFile(context: Context, userId: Long): File {
        val dir = File(context.filesDir, DIR).apply { mkdirs() }
        return File(dir, "${userId}_$FILE_NAME")
    }

    suspend fun saveFromUri(context: Context, userId: Long, uri: Uri): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val outFile = avatarFile(context, userId)
                outFile.parentFile?.mkdirs()
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(outFile).use { output -> input.copyTo(output) }
                } ?: return@withContext false
                true
            } catch (_: Exception) {
                false
            }
        }

    fun delete(context: Context, userId: Long) {
        avatarFile(context, userId).delete()
    }

    fun decodeForDisplay(path: String, maxSidePx: Int = 720): android.graphics.Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        while (longest / sample > maxSidePx) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(path, opts)
    }
}
