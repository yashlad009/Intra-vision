package com.example.aiinterviewcoach.ui.aptitude

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.AptitudeRepository
import com.example.aiinterviewcoach.model.QuestionData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.IOException
import javax.inject.Inject

data class PracticeState(
    val questions: List<QuestionData> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val showFinishedScreen: Boolean = false
)

@HiltViewModel
class PracticeQAViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AptitudeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PracticeState())
    val state: StateFlow<PracticeState> = _state

    fun loadQuestions(category: String, topicId: String) {
        viewModelScope.launch {
            val questionsList = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                repository.loadQuestions(category, topicId)
            }
            _state.value = PracticeState(questions = questionsList)
        }
    }

    fun flipCard() {
        val curr = _state.value
        _state.value = curr.copy(isFlipped = !curr.isFlipped)
    }

    fun previousQuestion() {
        val curr = _state.value
        if (curr.currentIndex > 0) {
            _state.value = curr.copy(
                currentIndex = curr.currentIndex - 1,
                isFlipped = false
            )
        }
    }

    fun nextQuestion() {
        val curr = _state.value
        if (curr.currentIndex + 1 < curr.questions.size) {
            _state.value = curr.copy(
                currentIndex = curr.currentIndex + 1,
                isFlipped = false
            )
        } else {
            // Completed all questions in the set
            _state.value = curr.copy(showFinishedScreen = true)
            viewModelScope.launch {
                repository.awardPracticeXp() // Practice completed: +10 XP
            }
        }
    }
}
