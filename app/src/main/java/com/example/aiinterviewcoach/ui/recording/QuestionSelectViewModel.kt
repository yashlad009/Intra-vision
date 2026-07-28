package com.example.aiinterviewcoach.ui.recording

import androidx.lifecycle.ViewModel
import com.example.aiinterviewcoach.data.local.QuestionRepository
import com.example.aiinterviewcoach.model.Question
import com.example.aiinterviewcoach.model.QuestionCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class QuestionSelectViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()

    fun loadQuestionsByCategory(categoryStr: String) {
        val category = try {
            QuestionCategory.valueOf(categoryStr.uppercase())
        } catch (e: Exception) {
            QuestionCategory.HR
        }
        _questions.value = questionRepository.getQuestionsByCategory(category)
    }
}
