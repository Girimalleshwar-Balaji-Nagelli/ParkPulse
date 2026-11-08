package com.parkpulse.app.data.sync

import com.parkpulse.app.data.local.dao.SurveyDao
import com.parkpulse.app.data.local.entity.SurveyEntity
import com.parkpulse.app.domain.model.SyncStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SurveySyncSource @Inject constructor(
    private val dao: SurveyDao,
    private val api: SyncApi,
) : SyncSource {

    override val label = "Surveys"
    override val syncOrder = 20 // after parks

    override fun observePendingCount(): Flow<Int> =
        dao.observeCountBySyncStatus(SyncStatus.PENDING.name)

    override suspend fun pendingCountNow(): Int = dao.countBySyncStatus(SyncStatus.PENDING.name)

    override suspend fun pushPending(onRecordSynced: () -> Unit): SyncOutcome {
        val pending = dao.getBySyncStatus(SyncStatus.PENDING.name)
        var synced = 0
        for (entity in pending) {
            try {
                api.upsertSurvey(entity.toDto())                                  // idempotent by id
                dao.updateSyncStatus(entity.id, SyncStatus.SYNCED.name)           // only after success
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

    private fun SurveyEntity.toDto() = SurveySyncDto(
        id = id,
        parkId = parkId,
        checkInTimeMillis = checkInTimeMillis,
        checkInLatitude = checkInLatitude,
        checkInLongitude = checkInLongitude,
        checkInAccuracyM = checkInAccuracyM,
        treeCount = treeCount,
        lightCount = lightCount,
        cleanliness = cleanliness,
        notes = notes,
        createdAtMillis = createdAtMillis,
    )
}
