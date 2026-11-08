package com.parkpulse.app.data.repository

import com.parkpulse.app.data.local.database.ParkDatabase
import com.parkpulse.app.data.local.dao.SurveyDao
import com.parkpulse.app.domain.repository.SurveyRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SurveyBindingsModule {
    @Binds
    abstract fun bindSurveyRepository(impl: SurveyRepositoryImpl): SurveyRepository
}

@Module
@InstallIn(SingletonComponent::class)
object SurveyDaoModule {
    @Provides
    fun provideSurveyDao(db: ParkDatabase): SurveyDao = db.surveyDao()
}