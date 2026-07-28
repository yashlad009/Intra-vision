package com.example.aiinterviewcoach.ui.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.RecordingRepository
import com.example.aiinterviewcoach.model.RecordingEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val currentSession: RecordingEntry? = null,
    val recommendations: String = "",
    val pastSessions: List<RecordingEntry> = emptyList()
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: RecordingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun loadSessionReport(filename: String) {
        viewModelScope.launch {
            val session = repository.getByFilename(filename)
            val allSessions = repository.getAllEntries().firstOrNull() ?: emptyList()
            
            // Sort past sessions chronologically (oldest to newest) for chart plotting
            val chronologicalSessions = allSessions.sortedBy { it.createdAt }

            val recommendationsText = generateRecommendations(session)

            _uiState.value = ReportUiState(
                currentSession = session,
                recommendations = recommendationsText,
                pastSessions = chronologicalSessions
            )
        }
    }

    private fun generateRecommendations(entry: RecordingEntry?): String {
        if (entry == null) return "No data available."
        
        val tips = mutableListOf<String>()

        if (entry.eyeContactScore < 70) {
            tips.add("• Look directly at the camera lens (not the screen) to show engagement and maintain eye contact.")
        }
        if (entry.postureScore < 75) {
            tips.add("• Sit up straight, keep your shoulders level, and avoid leaning or slouching in front of the camera.")
        }
        if (entry.speechClarityScore < 80) {
            tips.add("• Focus on clear enunciation and adjust your mic settings if needed to improve speech clarity.")
        }
        if (entry.paceScore < 70) {
            tips.add("• Pause naturally between sentences and moderate your talking speed to make your answer easier to follow.")
        }

        if (tips.isEmpty()) {
            tips.add("• Outstanding work! Your body language, eye contact, and vocal pace were well-controlled. Keep practicing!")
        }

        return tips.joinToString("\n")
    }
}
