package com.example.aiinterviewcoach.ui.aptitude

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.AptitudeRepository
import com.example.aiinterviewcoach.data.local.MockTestRepository
import com.example.aiinterviewcoach.model.QuestionData
import com.example.aiinterviewcoach.model.MockTestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class MockTestState(
    val questions: List<QuestionData> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswers: Map<Int, String> = emptyMap(), // Key: question index, Value: selected option text
    val timeRemainingSeconds: Int = 0,
    val isFinished: Boolean = false,
    val correctAnswersCount: Int = 0,
    val scorePercent: Double = 0.0,
    val totalTimeTakenSeconds: Int = 0
)

@HiltViewModel
class MockTestViewModel @Inject constructor(
    private val aptitudeRepository: AptitudeRepository,
    private val mockTestRepository: MockTestRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MockTestState())
    val state: StateFlow<MockTestState> = _state

    private var timerJob: Job? = null
    private var totalDurationSeconds = 0

    fun startTest(category: String, questionCount: Int) {
        viewModelScope.launch {
            val allQuestions = withContext(Dispatchers.IO) {
                aptitudeRepository.loadQuestions(category, "all")
            }
            val selectedQuestions = allQuestions.take(questionCount)
            val duration = questionCount * 45 // 45 seconds per question
            totalDurationSeconds = duration

            _state.value = MockTestState(
                questions = selectedQuestions,
                timeRemainingSeconds = duration
            )

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.timeRemainingSeconds > 0 && !_state.value.isFinished) {
                delay(1000)
                _state.value = _state.value.copy(
                    timeRemainingSeconds = _state.value.timeRemainingSeconds - 1
                )
            }
            if (!_state.value.isFinished) {
                submitTest()
            }
        }
    }

    fun selectAnswer(optionText: String) {
        val curr = _state.value
        val updatedAnswers = curr.selectedAnswers.toMutableMap()
        updatedAnswers[curr.currentIndex] = optionText
        _state.value = curr.copy(selectedAnswers = updatedAnswers)
    }

    fun nextQuestion() {
        val curr = _state.value
        if (curr.currentIndex + 1 < curr.questions.size) {
            _state.value = curr.copy(currentIndex = curr.currentIndex + 1)
        }
    }

    fun previousQuestion() {
        val curr = _state.value
        if (curr.currentIndex > 0) {
            _state.value = curr.copy(currentIndex = curr.currentIndex - 1)
        }
    }

    fun submitTest() {
        if (_state.value.isFinished) return
        timerJob?.cancel()

        val curr = _state.value
        val totalQuestions = curr.questions.size
        if (totalQuestions == 0) return

        val gradeResult = gradeMockTest(curr.questions, curr.selectedAnswers)
        val correctCount = gradeResult.first
        val percent = gradeResult.second
        val timeTaken = totalDurationSeconds - curr.timeRemainingSeconds

        _state.value = curr.copy(
            isFinished = true,
            correctAnswersCount = correctCount,
            scorePercent = percent,
            totalTimeTakenSeconds = timeTaken
        )

        viewModelScope.launch {
            val resultEntity = MockTestResult(
                category = if (totalQuestions > 0) "Mixed" else "", // category text to save
                totalQuestions = totalQuestions,
                correctAnswers = correctCount,
                scorePercent = percent,
                timeTakenSeconds = timeTaken,
                completedAt = System.currentTimeMillis()
            )
            // Save to DB
            withContext(Dispatchers.IO) {
                mockTestRepository.saveResult(resultEntity)
            }
        }
    }

    /**
     * Pure function to grade MCQ questions given correct key answers and user answers.
     * Returns Pair(correctAnswersCount, scorePercentage)
     */
    fun gradeMockTest(questions: List<QuestionData>, answers: Map<Int, String>): Pair<Int, Double> {
        if (questions.isEmpty()) return Pair(0, 0.0)
        var correctCount = 0
        for ((index, q) in questions.withIndex()) {
            val userAnswer = answers[index]
            // We trim and compare ignoring case to be safe, but standard is exact match or same option content
            if (userAnswer != null && userAnswer.trim().lowercase() == q.answer.trim().lowercase()) {
                correctCount++
            }
        }
        val percentage = (correctCount.toDouble() / questions.size.toDouble()) * 100.0
        return Pair(correctCount, percentage)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
