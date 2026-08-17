package com.example.aiinterviewcoach.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aiinterviewcoach.model.MockTestResult
import kotlinx.coroutines.flow.Flow

@Dao
interface MockTestDao {

    @Query("SELECT * FROM mock_test_results ORDER BY completedAt DESC")
    fun getAllMockTestResultsFlow(): Flow<List<MockTestResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTestResult(result: MockTestResult): Long

    @Query("SELECT * FROM mock_test_results WHERE category = :category ORDER BY completedAt DESC")
    suspend fun getResultsForCategory(category: String): List<MockTestResult>
}
