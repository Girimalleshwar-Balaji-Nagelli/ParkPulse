package com.parkpulse.app.data.photo

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-private, permanent photo storage: files/survey_photos/{surveyId}/{photoId}.jpg
 * (filesDir, never cache). The database stores only the relative path "{surveyId}/{photoId}.jpg"
 * (column `fileName`); older flat names such as "photo_x.jpg" still resolve.
 */
@Singleton
class PhotoStorage(private val dir: File) {

    @Inject
    constructor(@ApplicationContext context: Context) : this(File(context.filesDir, "survey_photos"))

    init {
        dir.mkdirs()
    }

    /** Resolves a stored relative path. Throws IllegalArgumentException if it would leave the photo directory. */
    fun fileFor(relativePath: String): File {
        val root = dir.canonicalFile
        val file = File(root, relativePath).canonicalFile
        require(file.path.startsWith(root.path + File.separator)) { "Invalid photo path" }
        return file
    }

    /**
     * Writes the (already stamped) bitmap as JPEG to {surveyId}/{photoId}.jpg and returns the
     * relative path. Written to a temp file, flushed to disk, then renamed, so a crash can never
     * leave a half-written photo under the final name. Call off the main thread.
     */
    fun saveJpeg(surveyId: String, photoId: String, bitmap: Bitmap, quality: Int = 90): String {
        require(SAFE_ID.matches(surveyId) && SAFE_ID.matches(photoId)) { "Invalid id" }
        val relative = "$surveyId/$photoId.jpg"
        val target = fileFor(relative)
        target.parentFile?.mkdirs()
        val tmp = File(target.parentFile, "${target.name}.tmp")
        try {
            FileOutputStream(tmp).use { out ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) { "JPEG compression failed" }
                out.flush()
                out.fd.sync()
            }
            check(tmp.length() > 0) { "Photo file is empty" }
            if (!tmp.renameTo(target)) {
                tmp.copyTo(target, overwrite = true)
            }
        } finally {
            tmp.delete()
        }
        return relative
    }

    fun delete(relativePath: String) {
        runCatching { fileFor(relativePath).delete() }
    }

    /** Removes every file of one survey (used when a draft survey is abandoned). */
    fun deleteSurveyDir(surveyId: String) {
        if (!SAFE_ID.matches(surveyId)) return
        runCatching { fileFor(surveyId).deleteRecursively() }
    }

    private companion object {
        val SAFE_ID = Regex("[A-Za-z0-9_-]+")
    }
}
