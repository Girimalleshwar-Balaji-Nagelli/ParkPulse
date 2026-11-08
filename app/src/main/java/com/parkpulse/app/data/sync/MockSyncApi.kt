package com.parkpulse.app.data.sync

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockSyncApi(
    private val root: File,
    private val latencyMs: Long = LATENCY_MS,
) : SyncApi {

    @Inject
    constructor(@ApplicationContext context: Context) : this(File(context.filesDir, "mock_backend"))

    private val _injectedFailures = MutableStateFlow(0)

    /** Number of upcoming requests that will fail (test hook). */
    val injectedFailures: StateFlow<Int> = _injectedFailures

    private val acceptedPhotoWrites = AtomicInteger(0)

    /** How many photo uploads the server accepted since the app started (a repeat of an id still counts as a write). */
    val photoWriteCount: Int get() = acceptedPhotoWrites.get()

    val storedParkCount: Int get() = count(File(root, "parks"))
    val storedSurveyCount: Int get() = count(File(root, "surveys"))
    val storedPhotoCount: Int
        get() = File(root, "photos").listFiles()?.sumOf { dir -> dir.listFiles { f -> f.extension == "jpg" }?.size ?: 0 } ?: 0

    fun serverPhotoFile(surveyId: String, photoId: String): File = File(root, "photos/$surveyId/$photoId.jpg")

    fun failNext(count: Int = 1) {
        _injectedFailures.value = count
    }

    override suspend fun upsertPark(park: ParkSyncDto) {
        simulateRequest()
        val boundary = park.boundary?.joinToString(";") { "${it[0]},${it[1]}" }.orEmpty()
        writeProps(
            File(root, "parks/${safe(park.id)}.properties"),
            mapOf(
                "id" to park.id, "name" to park.name, "type" to park.type, "areaSqM" to park.areaSqM.toString(),
                "boundary" to boundary, "lastSurveyDate" to park.lastSurveyDate, "status" to park.status,
            ),
        )
    }

    override suspend fun upsertSurvey(survey: SurveySyncDto) {
        simulateRequest()
        writeProps(
            File(root, "surveys/${safe(survey.id)}.properties"),
            mapOf(
                "id" to survey.id, "parkId" to survey.parkId,
                "checkInTimeMillis" to survey.checkInTimeMillis.toString(),
                "checkInLatitude" to survey.checkInLatitude.toString(),
                "checkInLongitude" to survey.checkInLongitude.toString(),
                "checkInAccuracyM" to survey.checkInAccuracyM.toString(),
                "treeCount" to survey.treeCount.toString(), "lightCount" to survey.lightCount.toString(),
                "cleanliness" to survey.cleanliness, "notes" to survey.notes,
                "createdAtMillis" to survey.createdAtMillis.toString(),
            ),
        )
    }

    override suspend fun upsertPhoto(photo: PhotoSyncDto, bytes: ByteArray) {
        simulateRequest()
        if (!File(root, "surveys/${safe(photo.surveyId)}.properties").exists()) {
            throw IOException("HTTP 409: survey ${photo.surveyId} is not on the server yet")
        }
        if (bytes.size.toLong() != photo.sizeBytes || Checksum.sha256Hex(bytes) != photo.sha256) {
            throw IOException("HTTP 422: upload corrupted (size or checksum mismatch)")
        }
        if (bytes.size < 2 || bytes[0] != 0xFF.toByte() || bytes[1] != 0xD8.toByte()) {
            throw IOException("HTTP 415: not a JPEG")
        }
        val dir = File(root, "photos/${safe(photo.surveyId)}")
        writeAtomic(File(dir, "${safe(photo.id)}.jpg")) { it.write(bytes) }
        writeProps(
            File(dir, "${safe(photo.id)}.properties"),
            mapOf(
                "id" to photo.id, "surveyId" to photo.surveyId, "fileName" to photo.fileName,
                "takenAtMillis" to photo.takenAtMillis.toString(),
                "latitude" to photo.latitude.toString(), "longitude" to photo.longitude.toString(),
                "accuracyM" to photo.accuracyM.toString(),
                "sizeBytes" to photo.sizeBytes.toString(), "sha256" to photo.sha256,
                "createdAtMillis" to photo.createdAtMillis.toString(),
            ),
        )
        acceptedPhotoWrites.incrementAndGet()
    }

    private suspend fun simulateRequest() {
        delay(latencyMs)
        if (_injectedFailures.value > 0) {
            _injectedFailures.update { (it - 1).coerceAtLeast(0) }
            throw IOException("Mock server unavailable (HTTP 503)")
        }
    }

    private fun safe(id: String): String {
        if (!SAFE_ID.matches(id)) throw IOException("HTTP 400: invalid id")
        return id
    }

    private fun count(dir: File): Int = dir.listFiles { f -> f.name.endsWith(".properties") }?.size ?: 0

    private fun writeProps(target: File, values: Map<String, String>) {
        val props = Properties().apply { values.forEach { (k, v) -> setProperty(k, v) } }
        writeAtomic(target) { props.store(it, null) }
    }

    /** Temp file + rename: a crash never leaves a half-written record under its real name. */
    private fun writeAtomic(target: File, write: (java.io.OutputStream) -> Unit) {
        target.parentFile?.mkdirs()
        val tmp = File(target.parentFile, "${target.name}.tmp")
        try {
            FileOutputStream(tmp).use { out ->
                write(out)
                out.flush()
                out.fd.sync()
            }
            if (!tmp.renameTo(target)) tmp.copyTo(target, overwrite = true)
        } finally {
            tmp.delete()
        }
    }

    private companion object {
        const val LATENCY_MS = 500L
        val SAFE_ID = Regex("[A-Za-z0-9_-]+")
    }
}
