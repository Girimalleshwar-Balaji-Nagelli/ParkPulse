package com.parkpulse.app.data.sync

import com.parkpulse.app.data.local.dao.SurveyDao
import com.parkpulse.app.data.local.entity.SurveyPhotoEntity
import com.parkpulse.app.data.photo.PhotoStorage
import com.parkpulse.app.domain.model.SyncStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoSyncSource @Inject constructor(
    private val dao: SurveyDao,
    private val api: SyncApi,
    private val storage: PhotoStorage,
) : SyncSource {

    override val label = "Photos"
    override val syncOrder = 30 // after surveys: a photo record references its survey

    override fun observePendingCount(): Flow<Int> =
        dao.observePhotoCountBySyncStatus(SyncStatus.PENDING.name)

    override suspend fun pendingCountNow(): Int = dao.countPhotosBySyncStatus(SyncStatus.PENDING.name)

    override suspend fun pushPending(onRecordSynced: () -> Unit): SyncOutcome {
        val pending = dao.getPhotosBySyncStatus(SyncStatus.PENDING.name)
        var synced = 0
        for (entity in pending) {
            try {
                val file = storage.fileFor(entity.fileName)
                if (!file.exists()) throw IOException("Photo file missing: ${entity.fileName}")
                api.upsertPhoto(entity.toDto(file.length()))                      // idempotent by id
                dao.updatePhotoSyncStatus(entity.id, SyncStatus.SYNCED.name)      // only after success
                synced++
                onRecordSynced()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return SyncOutcome(synced, pending.size - synced, e.message ?: "Sync failed")
            }
        }
        return SyncOutcome(synced, 0, null)
    }

    private fun SurveyPhotoEntity.toDto(sizeBytes: Long) = PhotoSyncDto(
        id = id,
        surveyId = surveyId,
        fileName = fileName,
        takenAtMillis = takenAtMillis,
        latitude = latitude,
        longitude = longitude,
        accuracyM = accuracyM,
        sizeBytes = sizeBytes,
        createdAtMillis = createdAtMillis,
    )
}
