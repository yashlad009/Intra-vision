package com.example.aiinterviewcoach.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mock_test_results")
data class MockTestResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val scorePercent: Double,
    val timeTakenSeconds: Int,
    val completedAt: Long
)
