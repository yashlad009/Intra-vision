package com.example.aiinterviewcoach.data.local

import com.example.aiinterviewcoach.model.ResumeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResumeRepository @Inject constructor(
    private val resumeDao: ResumeDao
) {
    val latestResume: Flow<ResumeEntity?> = resumeDao.getLatestResume()

    suspend fun getLatestResumeSync(): ResumeEntity? {
        return resumeDao.getLatestResumeSync()
    }

    suspend fun saveResume(fileName: String, extractedText: String, analysisJson: String): Long {
        // Clear previous resumes to maintain latest single resume
        resumeDao.deleteAllResumes()
        val entry = ResumeEntity(
            fileName = fileName,
            uploadedAt = System.currentTimeMillis(),
            extractedText = extractedText,
            analysisJson = analysisJson
        )
        return resumeDao.insertResume(entry)
    }

    suspend fun clearResume() {
        resumeDao.deleteAllResumes()
    }
}
