package com.example.aiinterviewcoach.model

enum class QuestionCategory {
    HR,
    TECHNICAL,
    BEHAVIORAL
}

data class Question(
    val id: Int,
    val text: String,
    val category: QuestionCategory,
    val tip: String
)
