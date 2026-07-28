package com.example.aiinterviewcoach.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aiinterviewcoach.model.RecordingEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {

    @Query("SELECT * FROM recording_entries WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllOrderedByDate(): Flow<List<RecordingEntry>>

    @Query("SELECT * FROM recording_entries WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getRecycleBinEntries(): Flow<List<RecordingEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: RecordingEntry)

    @Query("SELECT * FROM recording_entries WHERE filename = :filename LIMIT 1")
    suspend fun getByFilename(filename: String): RecordingEntry?

    @Query("UPDATE recording_entries SET isDeleted = 1, deletedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE recording_entries SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM recording_entries WHERE id = :id")
    suspend fun deletePermanently(id: Long)

    @Query("SELECT * FROM recording_entries WHERE isDeleted = 1")
    suspend fun getRecycleBinEntriesList(): List<RecordingEntry>

    @Query("DELETE FROM recording_entries WHERE isDeleted = 1")
    suspend fun emptyRecycleBin()
}
