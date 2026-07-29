package com.example.aiinterviewcoach.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.AptitudeRepository
import com.example.aiinterviewcoach.data.local.RecordingRepository
import com.example.aiinterviewcoach.data.local.ResumeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HomeProgressState(
    val quantitativeProgress: Int = 0,
    val logicalProgress: Int = 0,
    val verbalProgress: Int = 0,
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val totalSessions: Int = 0,
    val lastSessionTimeFormatted: String = "Never",
    val isResumeUploaded: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val aptitudeRepository: AptitudeRepository,
    private val recordingRepository: RecordingRepository,
    private val resumeRepository: ResumeRepository
) : ViewModel() {

    val uiState: StateFlow<HomeProgressState> = combine(
        aptitudeRepository.getAllProgress(),
        recordingRepository.getAllEntries(),
        resumeRepository.latestResume
    ) { progressList, recordingEntries, latestResume ->
        val quantCompleted = progressList.filter { it.category == "quantitative" && it.isCompleted }.size
        val logicalCompleted = progressList.filter { it.category == "logical" && it.isCompleted }.size
        val verbalCompleted = progressList.filter { it.category == "verbal" && it.isCompleted }.size

        val quantPercent = (quantCompleted * 100) / 14
        val logicalPercent = (logicalCompleted * 100) / 10
        val verbalPercent = (verbalCompleted * 100) / 8

        val totalXP = aptitudeRepository.getTotalXp()
        val aptitudeStreak = aptitudeRepository.getStreakCount()
        
        val totalSessions = recordingEntries.size
        val lastTimeFormatted = if (recordingEntries.isNotEmpty()) {
            val latestTimestamp = recordingEntries.maxOf { it.createdAt }
            formatTimeAgo(latestTimestamp)
        } else {
            "Never"
        }

        HomeProgressState(
            quantitativeProgress = quantPercent.coerceIn(0, 100),
            logicalProgress = logicalPercent.coerceIn(0, 100),
            verbalProgress = verbalPercent.coerceIn(0, 100),
            totalXp = totalXP,
            streakDays = aptitudeStreak,
            totalSessions = totalSessions,
            lastSessionTimeFormatted = lastTimeFormatted,
            isResumeUploaded = latestResume != null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeProgressState(
            totalXp = aptitudeRepository.getTotalXp(),
            streakDays = aptitudeRepository.getStreakCount()
        )
    )

    private fun formatTimeAgo(timeMs: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeMs
        if (diff < 0) return "Just now"

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes ${if (minutes == 1L) "min" else "mins"} ago"
            hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            days < 7 -> "$days ${if (days == 1L) "day" else "days"} ago"
            else -> {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                sdf.format(Date(timeMs))
            }
        }
    }
}
