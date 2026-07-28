package com.example.aiinterviewcoach.data.local

import com.example.aiinterviewcoach.model.RecordingEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecordingRepository @Inject constructor(
    private val dao: RecordingDao,
    private val storageHelper: LocalStorageHelper
) {
    /** Active non-deleted entries ordered by createdAt DESC */
    fun getAllEntries(): Flow<List<RecordingEntry>> = dao.getAllOrderedByDate()

    /** Deleted entries inside Recycle Bin ordered by deletedAt DESC */
    fun getRecycleBinEntries(): Flow<List<RecordingEntry>> = dao.getRecycleBinEntries()

    suspend fun insert(entry: RecordingEntry) = dao.insert(entry)

    suspend fun getByFilename(filename: String): RecordingEntry? = dao.getByFilename(filename)

    suspend fun softDelete(id: Long) = dao.softDelete(id)

    suspend fun restore(id: Long) = dao.restore(id)

    suspend fun deletePermanently(entry: RecordingEntry) {
        dao.deletePermanently(entry.id)
        storageHelper.deleteRecordingFile(entry.filename)
    }

    suspend fun emptyRecycleBin() {
        val trashList = dao.getRecycleBinEntriesList()
        for (entry in trashList) {
            storageHelper.deleteRecordingFile(entry.filename)
        }
        dao.emptyRecycleBin()
    }

    fun getFormattedStorageUsage(): String {
        return storageHelper.formatSize(storageHelper.getStorageUsageBytes())
    }
}
