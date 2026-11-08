package com.parkpulse.app.data.sync

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    abstract fun bindSyncApi(impl: MockSyncApi): SyncApi

    @Binds
    abstract fun bindNetworkMonitor(impl: ConnectivityNetworkMonitor): NetworkMonitor

    @Binds
    @IntoSet
    abstract fun bindParkSource(impl: ParkSyncSource): SyncSource

    @Binds
    @IntoSet
    abstract fun bindSurveySource(impl: SurveySyncSource): SyncSource

    @Binds
    @IntoSet
    abstract fun bindPhotoSource(impl: PhotoSyncSource): SyncSource
}
