package com.example.aiinterviewcoach.ui.aptitude

import com.example.aiinterviewcoach.data.local.AptitudeRepository
import com.example.aiinterviewcoach.data.local.MockTestRepository
import com.example.aiinterviewcoach.model.QuestionData
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.kotest.matchers.doubles.plusOrMinus

class MockTestGradingTest : StringSpec({

    "grading function correct count and percentage calculation" {
        val aptitudeRepo = mockk<AptitudeRepository>()
        val mockTestRepo = mockk<MockTestRepository>()
        val viewModel = MockTestViewModel(aptitudeRepo, mockTestRepo)

        val questions = listOf(
            QuestionData("Q1", listOf("A", "B"), "A", "E1"),
            QuestionData("Q2", listOf("A", "B"), "B", "E2"),
            QuestionData("Q3", listOf("A", "B"), "A", "E3"),
            QuestionData("Q4", listOf("A", "B"), "B", "E4")
        )

        // 1. All correct
        val answers1 = mapOf(0 to "A", 1 to "B", 2 to "A", 3 to "B")
        val result1 = viewModel.gradeMockTest(questions, answers1)
        result1.first shouldBe 4
        result1.second shouldBe (100.0 plusOrMinus 0.01)

        // 2. Partial correct
        val answers2 = mapOf(0 to "A", 1 to "Wrong", 2 to "A", 3 to "Wrong")
        val result2 = viewModel.gradeMockTest(questions, answers2)
        result2.first shouldBe 2
        result2.second shouldBe (50.0 plusOrMinus 0.01)

        // 3. Some unanswered (unanswered options at timeout)
        val answers3 = mapOf(0 to "A", 1 to "B") // remaining are null/missing
        val result3 = viewModel.gradeMockTest(questions, answers3)
        result3.first shouldBe 2
        result3.second shouldBe (50.0 plusOrMinus 0.01)

        // 4. Empty questions
        val resultEmpty = viewModel.gradeMockTest(emptyList(), emptyMap())
        resultEmpty.first shouldBe 0
        resultEmpty.second shouldBe (0.0 plusOrMinus 0.01)
    }
})
