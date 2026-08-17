package com.example.aiinterviewcoach.data.local

import com.example.aiinterviewcoach.model.MockTestResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockTestRepository @Inject constructor(
    private val dao: MockTestDao,
    private val appPrefs: AppPrefs
) {
    fun getAllResults(): Flow<List<MockTestResult>> = dao.getAllMockTestResultsFlow()

    suspend fun saveResult(result: MockTestResult): Long {
        val id = dao.insertMockTestResult(result)
        appPrefs.addXp(20) // Finish mock test: +20 XP
        return id
    }

    suspend fun getResultsForCategory(category: String): List<MockTestResult> {
        return dao.getResultsForCategory(category)
    }
}
