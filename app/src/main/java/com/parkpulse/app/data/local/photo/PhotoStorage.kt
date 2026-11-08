package com.parkpulse.app.data.photo

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** App-private, permanent photo storage (filesDir, not cache). Records store only the file name. */
@Singleton
class PhotoStorage @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dir = File(context.filesDir, "survey_photos").apply { mkdirs() }

    fun fileFor(fileName: String): File = File(dir, fileName)

    /** Writes the (already stamped) bitmap as JPEG and returns the file name. Call off the main thread. */
    fun saveJpeg(bitmap: Bitmap, quality: Int = 90): String {
        val name = "photo_${UUID.randomUUID()}.jpg"
        fileFor(name).outputStream().use { out ->
            check(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) { "JPEG compression failed" }
        }
        return name
    }

    fun delete(fileName: String) {
        fileFor(fileName).delete()
    }
}
