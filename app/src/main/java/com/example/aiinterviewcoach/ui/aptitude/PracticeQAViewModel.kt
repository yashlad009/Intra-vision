package com.example.aiinterviewcoach.ui.aptitude

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.AptitudeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.IOException
import javax.inject.Inject

data class QuestionData(
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String
)

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
            val questionsList = mutableListOf<QuestionData>()
            
            if (topicId.isEmpty() || topicId.lowercase() == "all") {
                val topics = repository.getTopicsForCategory(category)
                for (topic in topics) {
                    val jsonFileName = "prepare/$category/${topic.id}_questions.json"
                    try {
                        val jsonStr = context.assets.open(jsonFileName).bufferedReader().use { it.readText() }
                        val jsonArray = JSONArray(jsonStr)
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            val question = obj.optString("question", "")
                            val answer = obj.optString("answer", "")
                            val explanation = obj.optString("explanation", "")
                            
                            val optionsArray = obj.optJSONArray("options")
                            val options = mutableListOf<String>()
                            if (optionsArray != null) {
                                for (j in 0 until optionsArray.length()) {
                                    options.add(optionsArray.getString(j))
                                }
                            }
                            questionsList.add(QuestionData(question, options, answer, explanation))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                questionsList.shuffle()
            } else {
                val jsonFileName = "prepare/$category/${topicId}_questions.json"
                try {
                    val jsonStr = context.assets.open(jsonFileName).bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonStr)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val question = obj.optString("question", "")
                        val answer = obj.optString("answer", "")
                        val explanation = obj.optString("explanation", "")
                        
                        val optionsArray = obj.optJSONArray("options")
                        val options = mutableListOf<String>()
                        if (optionsArray != null) {
                            for (j in 0 until optionsArray.length()) {
                                  options.add(optionsArray.getString(j))
                            }
                        }
                        questionsList.add(QuestionData(question, options, answer, explanation))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _state.value = PracticeState(questions = questionsList)
        }
    }

    fun flipCard() {
        val curr = _state.value
        _state.value = curr.copy(isFlipped = !curr.isFlipped)
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
