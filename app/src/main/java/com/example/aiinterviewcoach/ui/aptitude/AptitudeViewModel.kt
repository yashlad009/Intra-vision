package com.example.aiinterviewcoach.ui.aptitude

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.AptitudeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CategoryProgressState(
    val quantitativeProgress: Int = 0, // percentage 0-100
    val logicalProgress: Int = 0,
    val verbalProgress: Int = 0,
    val totalXp: Int = 0,
    val streakDays: Int = 0
)

@HiltViewModel
class AptitudeViewModel @Inject constructor(
    private val repository: AptitudeRepository
) : ViewModel() {

    val uiState: StateFlow<CategoryProgressState> = repository.getAllProgress()
        .map { progressList ->
            val quantCompleted = progressList.filter { it.category == "quantitative" && it.isCompleted }.size
            val logicalCompleted = progressList.filter { it.category == "logical" && it.isCompleted }.size
            val verbalCompleted = progressList.filter { it.category == "verbal" && it.isCompleted }.size

            val quantPercent = (quantCompleted * 100) / 14
            val logicalPercent = (logicalCompleted * 100) / 10
            val verbalPercent = (verbalCompleted * 100) / 8

            CategoryProgressState(
                quantitativeProgress = quantPercent.coerceIn(0, 100),
                logicalProgress = logicalPercent.coerceIn(0, 100),
                verbalProgress = verbalPercent.coerceIn(0, 100),
                totalXp = repository.getTotalXp(),
                streakDays = repository.getStreakCount()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoryProgressState(
                totalXp = repository.getTotalXp(),
                streakDays = repository.getStreakCount()
            )
        )

    fun refreshStats() {
        // AppPrefs has in-memory state that updates instantly on adding XP or recording activity
    }
}
