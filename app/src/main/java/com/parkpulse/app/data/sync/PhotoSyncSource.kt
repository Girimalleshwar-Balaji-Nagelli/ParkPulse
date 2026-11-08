package com.parkpulse.app.data.sync

import com.parkpulse.app.data.local.dao.SurveyDao
import com.parkpulse.app.data.local.entity.SurveyPhotoEntity
import com.parkpulse.app.data.photo.PhotoStorage
import com.parkpulse.app.domain.model.SyncStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoSyncSource @Inject constructor(
    private val dao: SurveyDao,
    private val api: SyncApi,
    private val storage: PhotoStorage,
) : SyncSource {

    override val label = "Photos"
    override val syncOrder = 30 // after surveys: the server rejects a photo whose survey it does not have

    override fun observePendingCount(): Flow<Int> =
        dao.observePhotoCountBySyncStatus(SyncStatus.PENDING.name)

    override suspend fun pendingCountNow(): Int = dao.countPhotosBySyncStatus(SyncStatus.PENDING.name)

    override suspend fun pushPending(onRecordSynced: () -> Unit): SyncOutcome {
        val pending = dao.getPhotosBySyncStatus(SyncStatus.PENDING.name)
        var synced = 0
        var missingMessage: String? = null
        for (entity in pending) {
            try {
                val file: File? = try {
                    storage.fileFor(entity.fileName).takeIf { it.isFile }
                } catch (e: IllegalArgumentException) {
                    null
                }
                if (file == null) {
                    // Cannot be uploaded and never will be: keep it PENDING (never claim SYNCED) but
                    // do not let one lost file block every photo behind it.
                    if (missingMessage == null) missingMessage = "Photo file missing: ${entity.fileName}"
                    continue
                }
                val bytes = file.readBytes()
                api.upsertPhoto(entity.toDto(bytes), bytes)                        // 1. server stores bytes + metadata
                dao.updatePhotoSyncStatus(entity.id, SyncStatus.SYNCED.name)       // 2. only now flip the local flag
                synced++
                onRecordSynced()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Network or server failure: stop the run, everything unsent stays PENDING for the retry.
                return SyncOutcome(synced, pending.size - synced, e.message ?: "Sync failed")
            }
        }
        return SyncOutcome(synced, pending.size - synced, missingMessage)
    }

    private fun SurveyPhotoEntity.toDto(bytes: ByteArray) = PhotoSyncDto(
        id = id,
        surveyId = surveyId,
        fileName = fileName,
        takenAtMillis = takenAtMillis,
        latitude = latitude,
        longitude = longitude,
        accuracyM = accuracyM,
        sizeBytes = bytes.size.toLong(),
        sha256 = Checksum.sha256Hex(bytes),
        createdAtMillis = createdAtMillis,
    )
}
