package com.example.aiinterviewcoach.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aiinterviewcoach.model.ResumeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeDao {

    @Query("SELECT * FROM resume_entries ORDER BY uploadedAt DESC LIMIT 1")
    fun getLatestResume(): Flow<ResumeEntity?>

    @Query("SELECT * FROM resume_entries ORDER BY uploadedAt DESC LIMIT 1")
    suspend fun getLatestResumeSync(): ResumeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: ResumeEntity): Long

    @Query("DELETE FROM resume_entries")
    suspend fun deleteAllResumes()
}
