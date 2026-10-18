package com.example.wildlife_app.data.local

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class CameraTarget(val path: String, val contentUri: Uri)

/** Stores sighting photos in the app's private storage. */
class PhotoStorage(context: Context) {

    private val appContext = context.applicationContext

    private fun photoDir(): File =
        File(appContext.filesDir, "photos").also { it.mkdirs() }

    private fun newPhotoFile(): File =
        File(photoDir(), "sighting_${System.currentTimeMillis()}.jpg")

    /** Copies a picked gallery image into app storage. Returns null if the copy fails. */
    suspend fun copyFromGallery(source: Uri): String? = withContext(Dispatchers.IO) {
        val target = newPhotoFile()
        try {
            val input = appContext.contentResolver.openInputStream(source)
                ?: return@withContext null
            input.use { stream ->
                target.outputStream().use { out -> stream.copyTo(out) }
            }
            Uri.fromFile(target).toString()
        } catch (e: IOException) {
            target.delete()
            null
        } catch (e: SecurityException) {
            target.delete()
            null
        }
    }

    /** Creates an empty file for the camera app to write into. */
    @Throws(IOException::class)
    fun createCameraTarget(): CameraTarget {
        val file = newPhotoFile()
        file.createNewFile()
        val uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file
        )
        return CameraTarget(file.absolutePath, uri)
    }

    /** Returns the stored photo URI if the capture succeeded, otherwise cleans up and returns null. */
    fun finishCameraCapture(path: String, success: Boolean): String? {
        val file = File(path)
        return if (success && file.exists() && file.length() > 0L) {
            Uri.fromFile(file).toString()
        } else {
            file.delete()
            null
        }
    }
}
