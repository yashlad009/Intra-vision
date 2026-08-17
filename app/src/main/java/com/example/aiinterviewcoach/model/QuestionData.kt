package com.example.aiinterviewcoach.model

data class QuestionData(
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String
)
