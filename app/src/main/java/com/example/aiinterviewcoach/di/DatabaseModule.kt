package com.example.aiinterviewcoach.di

import android.content.Context
import com.example.aiinterviewcoach.data.local.AppDatabase
import com.example.aiinterviewcoach.data.local.RecordingDao
import com.example.aiinterviewcoach.data.local.AptitudeDao
import com.example.aiinterviewcoach.data.local.ResumeDao
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
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.create(ctx)

    @Provides
    @Singleton
    fun provideRecordingDao(db: AppDatabase): RecordingDao = db.recordingDao()

    @Provides
    @Singleton
    fun provideAptitudeDao(db: AppDatabase): AptitudeDao = db.aptitudeDao()

    @Provides
    @Singleton
    fun provideResumeDao(db: AppDatabase): ResumeDao = db.resumeDao()
}

