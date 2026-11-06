package com.parkpulse.app.domain.usecase

import com.parkpulse.app.domain.model.Park
import com.parkpulse.app.domain.repository.ParkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveParksUseCase @Inject constructor(private val repository: ParkRepository) {
    operator fun invoke(): Flow<List<Park>> = repository.observeParks()
}

class SeedParksUseCase @Inject constructor(private val repository: ParkRepository) {
    suspend operator fun invoke() = repository.seedIfEmpty()
}

class AddParkUseCase @Inject constructor(private val repository: ParkRepository) {
    suspend operator fun invoke(park: Park) = repository.addPark(park)
}