package com.example.aiinterviewcoach.di

import android.content.Context
import com.example.aiinterviewcoach.data.auth.AuthRepository
import com.example.aiinterviewcoach.data.auth.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        @ApplicationContext context: Context
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, context)
}
