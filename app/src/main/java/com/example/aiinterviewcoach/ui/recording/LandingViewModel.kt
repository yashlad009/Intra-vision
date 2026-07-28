package com.example.aiinterviewcoach.ui.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.RecordingRepository
import com.example.aiinterviewcoach.model.RecordingEntry
import com.example.aiinterviewcoach.data.local.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

data class LandingUiState(
    val totalSessions: Int = 0,
    val lastSessionTimeFormatted: String = "Never",
    val streakText: String = "0 Days"
)

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val repository: RecordingRepository,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    fun getRandomQuestionText(): String {
        val questions = questionRepository.getAllQuestions()
        return if (questions.isNotEmpty()) {
            questions.random().text
        } else {
            "Tell me about yourself."
        }
    }

    val uiState: StateFlow<LandingUiState> = repository.getAllEntries()
        .map { entries ->
            val total = entries.size
            val lastTimeFormatted = if (entries.isNotEmpty()) {
                val latestTimestamp = entries.maxOf { it.createdAt }
                formatTimeAgo(latestTimestamp)
            } else {
                "Never"
            }
            val streak = calculateStreak(entries)
            LandingUiState(
                totalSessions = total,
                lastSessionTimeFormatted = lastTimeFormatted,
                streakText = "$streak ${if (streak == 1) "Day" else "Days"}"
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LandingUiState()
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

    private fun calculateStreak(entries: List<RecordingEntry>): Int {
        if (entries.isEmpty()) return 0

        // Parse timestamps into date strings "yyyy-MM-dd" to get unique days
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }

        val uniqueDates = entries.map { sdf.format(Date(it.createdAt)) }
            .distinct()
            .map { sdf.parse(it)!! }
            .sortedDescending() // newest to oldest

        if (uniqueDates.isEmpty()) return 0

        val cal = Calendar.getInstance().apply {
            timeZone = TimeZone.getDefault()
        }
        
        // Zero out time details of Calendar for today
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val today = cal.time

        // Yesterday
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.time

        val newestDate = uniqueDates.first()

        // If the newest practice is older than yesterday, streak is broken
        if (newestDate.before(yesterday) && newestDate != today) {
            return 0
        }

        var streak = 0
        cal.time = if (newestDate == today) today else yesterday

        for (date in uniqueDates) {
            val dateToCheck = cal.time
            // If it matches expected date in sequence
            if (date == dateToCheck || (date == today && dateToCheck == yesterday)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else if (date.before(dateToCheck)) {
                // Gap in streak
                break
            }
        }

        return streak
    }
}
