package com.parkpulse.app.di

import android.content.Context
import androidx.room.Room
import com.parkpulse.app.data.local.dao.ParkDao
import com.parkpulse.app.data.local.database.ParkDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ParkDatabase =
        Room.databaseBuilder(context, ParkDatabase::class.java, "parkpulse.db").build()

    @Provides
    fun provideParkDao(db: ParkDatabase): ParkDao = db.parkDao()
}