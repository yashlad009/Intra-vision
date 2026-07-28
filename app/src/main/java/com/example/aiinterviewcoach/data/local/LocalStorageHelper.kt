package com.example.aiinterviewcoach.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class LocalStorageHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recordingsDir: File
        get() = File(context.filesDir, "recordings").also { it.mkdirs() }

    /**
     * Returns a File in app-internal storage with name interview_<timestamp>.mp4.
     * Creates parent directories if absent.
     */
    fun createOutputFile(timestampMs: Long = System.currentTimeMillis()): File {
        return File(recordingsDir, "interview_${timestampMs}.mp4")
    }

    fun getFile(filename: String): File {
        return File(recordingsDir, filename)
    }

    fun deleteRecordingFile(filename: String): Boolean {
        val file = getFile(filename)
        return if (file.exists()) file.delete() else false
    }

    fun getStorageUsageBytes(): Long {
        val files = recordingsDir.listFiles() ?: return 0L
        return files.sumOf { it.length() }
    }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 MB"
        val mb = bytes / (1024.0 * 1024.0)
        return if (mb < 1.0) {
            val kb = bytes / 1024.0
            String.format("%.1f KB", kb)
        } else {
            String.format("%.1f MB", mb)
        }
    }

    fun purgeOrphanedFiles(validFilenames: Set<String>) {
        val files = recordingsDir.listFiles() ?: return
        for (file in files) {
            if (!validFilenames.contains(file.name)) {
                file.delete()
            }
        }
    }
}
