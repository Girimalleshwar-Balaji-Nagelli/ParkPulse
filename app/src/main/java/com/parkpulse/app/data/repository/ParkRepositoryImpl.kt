package com.parkpulse.app.data.repository

import com.parkpulse.app.data.local.dao.ParkDao
import com.parkpulse.app.data.local.entity.toDomain
import com.parkpulse.app.data.local.entity.toEntity
import com.parkpulse.app.data.local.seed.ParkSeedData
import com.parkpulse.app.domain.model.Park
import com.parkpulse.app.domain.repository.ParkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ParkRepositoryImpl @Inject constructor(
    private val dao: ParkDao
) : ParkRepository {

    override fun observeParks(): Flow<List<Park>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            dao.insertAll(ParkSeedData.parks.map { it.toEntity() })
        }
    }

    override suspend fun addPark(park: Park) {
        dao.insert(park.toEntity())
    }
}