package com.example.aiinterviewcoach.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recording_entries")
data class RecordingEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filename: String,
    val createdAt: Long,      // epoch-milliseconds from System.currentTimeMillis()
    val overallScore: Int = 0,
    val eyeContactScore: Int = 0,
    val postureScore: Int = 0,
    val speechClarityScore: Int = 0,
    val paceScore: Int = 0,
    val wordsPerMinute: Int = 0,
    val fillerWordCount: Int = 0,
    val durationSeconds: Long = 0,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null
)
