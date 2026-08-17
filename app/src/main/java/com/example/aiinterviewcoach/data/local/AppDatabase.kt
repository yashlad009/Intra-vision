package com.example.aiinterviewcoach.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.aiinterviewcoach.model.RecordingEntry
import com.example.aiinterviewcoach.model.AptitudeProgress
import com.example.aiinterviewcoach.model.ResumeEntity
import com.example.aiinterviewcoach.model.MockTestResult

@Database(entities = [RecordingEntry::class, AptitudeProgress::class, ResumeEntity::class, MockTestResult::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordingDao(): RecordingDao
    abstract fun aptitudeDao(): AptitudeDao
    abstract fun resumeDao(): ResumeDao
    abstract fun mockTestDao(): MockTestDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "interview_coach.db")
                .fallbackToDestructiveMigration() // safe for dev; revisit before release
                .build()
    }
}
