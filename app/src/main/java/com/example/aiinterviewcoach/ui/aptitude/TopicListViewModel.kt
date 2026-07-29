package com.example.aiinterviewcoach.ui.aptitude

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.AptitudeRepository
import com.example.aiinterviewcoach.data.local.AptitudeTopic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopicListItem(
    val topic: AptitudeTopic,
    val isCompleted: Boolean
)

@HiltViewModel
class TopicListViewModel @Inject constructor(
    private val repository: AptitudeRepository
) : ViewModel() {

    private val _topics = MutableStateFlow<List<AptitudeTopic>>(emptyList())

    val topicItems: StateFlow<List<TopicListItem>> = combine(_topics, repository.getAllProgress()) { topics, progressList ->
        topics.map { topic ->
            val progress = progressList.find { it.topicId == "${topic.category}/${topic.id}" }
            TopicListItem(topic, progress?.isCompleted ?: false)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun loadTopics(category: String) {
        viewModelScope.launch {
            val list = repository.getTopicsForCategory(category)
            _topics.value = list
        }
    }
}
