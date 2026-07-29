package com.example.aiinterviewcoach.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aiinterviewcoach.model.AptitudeProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface AptitudeDao {

    @Query("SELECT * FROM aptitude_progress")
    fun getAllProgressFlow(): Flow<List<AptitudeProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: AptitudeProgress)

    @Query("SELECT * FROM aptitude_progress WHERE topicId = :topicId LIMIT 1")
    suspend fun getProgressForTopic(topicId: String): AptitudeProgress?
}
