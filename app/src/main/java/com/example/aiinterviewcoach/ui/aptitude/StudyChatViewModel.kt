package com.example.aiinterviewcoach.ui.aptitude

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.chat.RetrievalScorer
import com.example.aiinterviewcoach.data.chat.StudyChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false
)

@HiltViewModel
class StudyChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatRepository: StudyChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state

    private var allSections: List<MarkdownSection> = emptyList()

    fun initTopic(category: String, topicId: String) {
        viewModelScope.launch {
            val mdFileName = "prepare/$category/$topicId.md"
            try {
                val markdownContent = withContext(Dispatchers.IO) {
                    context.assets.open(mdFileName).bufferedReader().use { it.readText() }
                }
                allSections = MarkdownSectionParser.parse(markdownContent)
                
                // Add initial welcome message
                _state.value = ChatState(
                    messages = listOf(
                        ChatMessage("Hello! Ask me any questions about this topic based on your study notes.", false)
                    )
                )
            } catch (e: IOException) {
                e.printStackTrace()
                _state.value = ChatState(
                    messages = listOf(
                        ChatMessage("Failed to load study notes context for this topic.", false)
                    )
                )
            }
        }
    }

    fun sendMessage(messageText: String) {
        if (messageText.trim().isEmpty() || _state.value.isLoading) return

        val userMessage = ChatMessage(messageText, true)
        val currentMessages = _state.value.messages + userMessage
        _state.value = _state.value.copy(
            messages = currentMessages,
            isLoading = true,
            isOffline = false
        )

        viewModelScope.launch {
            // Retrieve top-3 matching sections using the keyword TF-IDF retrieval scorer
            val scored = RetrievalScorer.scoreSections(allSections, messageText)
            val topKSections = scored.take(3).map { it.first }

            val responseResult = chatRepository.getChatResponse(topKSections, messageText)
            
            _state.value = _state.value.copy(isLoading = false)

            responseResult.fold(
                onSuccess = { reply ->
                    _state.value = _state.value.copy(
                        messages = _state.value.messages + ChatMessage(reply, false)
                    )
                },
                onFailure = { error ->
                    val isNetworkError = error is java.net.UnknownHostException || error is java.net.ConnectException
                    if (isNetworkError) {
                        _state.value = _state.value.copy(
                            isOffline = true,
                            messages = _state.value.messages + ChatMessage("You're offline — chat needs an internet connection.", false)
                        )
                    } else {
                        _state.value = _state.value.copy(
                            messages = _state.value.messages + ChatMessage("Error: ${error.localizedMessage ?: "Failed to generate reply"}", false)
                        )
                    }
                }
            )
        }
    }
}
