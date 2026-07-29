package com.example.aiinterviewcoach.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aptitude_progress")
data class AptitudeProgress(
    @PrimaryKey val topicId: String, // e.g. "quantitative/percentage"
    val category: String,          // e.g. "quantitative"
    val isCompleted: Boolean,
    val lastOpenedAt: Long
)
