package com.parkpulse.app.di

import com.parkpulse.app.data.repository.ParkRepositoryImpl
import com.parkpulse.app.domain.repository.ParkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ParkRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindParkRepository(impl: ParkRepositoryImpl): ParkRepository
}