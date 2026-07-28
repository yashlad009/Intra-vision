package com.example.aiinterviewcoach.ui.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.RecordingRepository
import com.example.aiinterviewcoach.model.RecordingEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HistoryTab {
    ACTIVE, RECYCLE_BIN
}

data class HistoryViewState(
    val selectedTab: HistoryTab = HistoryTab.ACTIVE,
    val activeEntries: List<RecordingEntry> = emptyList(),
    val recycleBinEntries: List<RecordingEntry> = emptyList(),
    val storageUsage: String = "0 MB",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: RecordingRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(HistoryTab.ACTIVE)
    val currentTab: StateFlow<HistoryTab> = _currentTab.asStateFlow()

    private val _storageUsage = MutableStateFlow(repository.getFormattedStorageUsage())
    val storageUsage: StateFlow<String> = _storageUsage.asStateFlow()

    val activeEntries: StateFlow<List<RecordingEntry>> = repository
        .getAllEntries()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val studentOverallScore: StateFlow<Int> = activeEntries
        .map { list ->
            if (list.isEmpty()) 0 else (list.sumOf { it.overallScore } / list.size)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val studentRatingBadge: StateFlow<String> = studentOverallScore
        .map { score ->
            when {
                score == 0 -> "No Data • Practice to Start"
                score >= 85 -> "Top Tier • Interview Ready 🌟"
                score >= 70 -> "Good Progress • Keep Practicing 🚀"
                score >= 50 -> "Moderate • Focus on Speech & Eye Contact 💡"
                else -> "Needs Improvement • Keep Going 💪"
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "No Data • Practice to Start")

    val recycleBinEntries: StateFlow<List<RecordingEntry>> = repository
        .getRecycleBinEntries()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectTab(tab: HistoryTab) {
        _currentTab.value = tab
    }

    fun softDelete(entry: RecordingEntry) {
        viewModelScope.launch {
            repository.softDelete(entry.id)
            updateStorageUsage()
        }
    }

    fun restore(entry: RecordingEntry) {
        viewModelScope.launch {
            repository.restore(entry.id)
            updateStorageUsage()
        }
    }

    fun deletePermanently(entry: RecordingEntry) {
        viewModelScope.launch {
            repository.deletePermanently(entry)
            updateStorageUsage()
        }
    }

    fun emptyRecycleBin() {
        viewModelScope.launch {
            repository.emptyRecycleBin()
            updateStorageUsage()
        }
    }

    fun updateStorageUsage() {
        _storageUsage.value = repository.getFormattedStorageUsage()
    }
}
