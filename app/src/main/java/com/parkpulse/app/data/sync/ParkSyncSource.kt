package com.parkpulse.app.data.sync

import com.parkpulse.app.data.local.dao.ParkDao
import com.parkpulse.app.data.local.entity.ParkEntity
import com.parkpulse.app.domain.model.SyncStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkSyncSource @Inject constructor(
    private val dao: ParkDao,
    private val api: SyncApi,
) : SyncSource {

    override val label = "Parks"

    override fun observePendingCount(): Flow<Int> = dao.observeCountBySyncStatus(SyncStatus.PENDING)

    override suspend fun pendingCountNow(): Int = dao.countBySyncStatus(SyncStatus.PENDING)

    override suspend fun pushPending(onRecordSynced: () -> Unit): SyncOutcome {
        val pending = dao.getBySyncStatus(SyncStatus.PENDING)
        var synced = 0
        for (entity in pending) {
            try {
                api.upsertPark(entity.toDto())                      // 1. server accepts (idempotent by id)
                dao.updateSyncStatus(entity.id, SyncStatus.SYNCED)  // 2. only then flip the local flag
                synced++
                onRecordSynced()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Record stays PENDING. If the crash happened between 1 and 2 the next run re-sends
                // the same id, which the server upserts, so nothing is duplicated.
                return SyncOutcome(synced, pending.size - synced, e.message ?: "Sync failed")
            }
        }
        return SyncOutcome(synced, 0, null)
    }

    private fun ParkEntity.toDto() = ParkSyncDto(
        id = id,
        name = name,
        type = type.name,
        areaSqM = areaSqM,
        boundary = boundary?.map { listOf(it.latitude, it.longitude) },
        lastSurveyDate = lastSurveyDate.toString(),
        status = status.name,
    )
}