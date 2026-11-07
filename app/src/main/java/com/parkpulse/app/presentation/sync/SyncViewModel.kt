package com.parkpulse.app.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parkpulse.app.data.sync.MockSyncApi
import com.parkpulse.app.data.sync.SyncManager
import com.parkpulse.app.data.sync.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SyncUiState(
    val state: SyncState = SyncState.Idle,
    val pending: Int = 0,
    val online: Boolean = true,
    val testFailureArmed: Boolean = false,
)

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val mockApi: MockSyncApi,
) : ViewModel() {

    val uiState: StateFlow<SyncUiState> = combine(
        syncManager.state,
        syncManager.pendingCount,
        syncManager.isOnline,
        mockApi.injectedFailures,
    ) { state, pending, online, failures ->
        SyncUiState(state, pending, online, failures > 0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncUiState())

    fun syncNow() = syncManager.requestSync()

    /** Long-press on the pill: the next mock-server request fails (for testing retry). */
    fun armTestFailure() = mockApi.failNext(1)
}