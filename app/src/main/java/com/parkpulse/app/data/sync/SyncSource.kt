package com.parkpulse.app.data.sync

import kotlinx.coroutines.flow.Flow

/**
 * One syncable record type. Parks and surveys implement it; photo records plug in the same way by
 * adding another implementation bound @IntoSet in SyncModule. SyncManager needs no changes.
 */
interface SyncSource {
    val label: String

    /** Lower runs first. Parents must sync before the records that reference them. */
    val syncOrder: Int get() = 100

    fun observePendingCount(): Flow<Int>
    suspend fun pendingCountNow(): Int

    /**
     * Pushes pending records one by one, marking each SYNCED only after the backend accepted it.
     * Stops at the first failure. Never deletes or modifies local data other than the status flag.
     */
    suspend fun pushPending(onRecordSynced: () -> Unit): SyncOutcome
}
