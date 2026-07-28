package com.example.aiinterviewcoach.di

import android.content.Context
import com.example.aiinterviewcoach.data.local.LocalStorageHelper
import com.example.aiinterviewcoach.data.local.RecordingDao
import com.example.aiinterviewcoach.data.local.RecordingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRecordingRepository(
        dao: RecordingDao,
        storageHelper: LocalStorageHelper
    ): RecordingRepository = RecordingRepository(dao, storageHelper)

    @Provides
    @Singleton
    fun provideLocalStorageHelper(@ApplicationContext ctx: Context): LocalStorageHelper =
        LocalStorageHelper(ctx)
}
