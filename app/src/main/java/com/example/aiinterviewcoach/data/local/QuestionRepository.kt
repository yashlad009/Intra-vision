package com.example.aiinterviewcoach.data.local

import com.example.aiinterviewcoach.model.Question
import com.example.aiinterviewcoach.model.QuestionCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor() {

    private val questions = listOf(
        // HR
        Question(
            id = 1,
            text = "Tell me about yourself.",
            category = QuestionCategory.HR,
            tip = "Summarize your professional background, highlight key achievements, and explain why you're a fit for this role."
        ),
        Question(
            id = 2,
            text = "Why do you want to work at this company?",
            category = QuestionCategory.HR,
            tip = "Demonstrate knowledge of the company's products/mission and connect them to your skills and career interests."
        ),
        Question(
            id = 3,
            text = "What are your greatest strengths and weaknesses?",
            category = QuestionCategory.HR,
            tip = "Highlight strengths relevant to the job, and discuss a genuine weakness along with steps you are taking to overcome it."
        ),

        // Technical
        Question(
            id = 4,
            text = "What is the difference between a Process and a Thread?",
            category = QuestionCategory.TECHNICAL,
            tip = "Processes run in separate memory spaces with resources, while threads share the process's memory space and are lightweight."
        ),
        Question(
            id = 5,
            text = "How does garbage collection work in Java/Kotlin?",
            category = QuestionCategory.TECHNICAL,
            tip = "Explain heap space, identification of unreachable objects (Mark-and-Sweep), and reclaiming memory automatically."
        ),
        Question(
            id = 6,
            text = "Explain the concept of Indexes in a Database.",
            category = QuestionCategory.TECHNICAL,
            tip = "An index is a structure (e.g. B-Tree) that speeds up data retrieval, but increases write times and requires additional storage."
        ),

        // Behavioral
        Question(
            id = 7,
            text = "Describe a time you faced a difficult challenge in a project and how you overcame it.",
            category = QuestionCategory.BEHAVIORAL,
            tip = "Use the STAR method: explain the Situation, describe your Task, detail the Action you took, and outline the quantitative Result."
        ),
        Question(
            id = 8,
            text = "Tell me about a time you had a conflict with a team member and how you resolved it.",
            category = QuestionCategory.BEHAVIORAL,
            tip = "Emphasize communication, objective listening, empathy, and how you worked together to find a mutually beneficial solution."
        ),
        Question(
            id = 9,
            text = "Describe a situation where you had to lead a team or initiative.",
            category = QuestionCategory.BEHAVIORAL,
            tip = "Focus on coordination, setting goals, delegation, motivating teammates, and achieving successful project completion."
        )
    )

    fun getQuestionsByCategory(category: QuestionCategory): List<Question> {
        return questions.filter { it.category == category }
    }

    fun getQuestionById(id: Int): Question? {
        return questions.find { it.id == id }
    }

    fun getAllQuestions(): List<Question> {
        return questions
    }
}
