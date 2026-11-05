package com.parkpulse.app.domain.repository

import com.parkpulse.app.domain.model.Park
import kotlinx.coroutines.flow.Flow

interface ParkRepository {
    fun observeParks(): Flow<List<Park>>
    suspend fun seedIfEmpty()
}