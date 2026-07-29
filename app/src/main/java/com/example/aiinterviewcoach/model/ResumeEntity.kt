package com.example.aiinterviewcoach.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resume_entries")
data class ResumeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val uploadedAt: Long,       // epoch-milliseconds from System.currentTimeMillis()
    val extractedText: String,
    val analysisJson: String
)
