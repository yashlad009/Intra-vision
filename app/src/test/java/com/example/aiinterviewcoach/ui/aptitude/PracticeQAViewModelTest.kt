package com.example.aiinterviewcoach.ui.aptitude

import android.content.Context
import android.content.res.AssetManager
import com.example.aiinterviewcoach.data.local.AptitudeRepository
import com.example.aiinterviewcoach.model.QuestionData
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import java.io.ByteArrayInputStream

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeQAViewModelTest : StringSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
    }

    afterSpec {
        Dispatchers.resetMain()
        unmockkStatic(Dispatchers::class)
    }

    "load 3 questions, call nextQuestion() twice, call previousQuestion() once, assert currentIndex == 1 and isFlipped == false" {
        runTest {
            val context = mockk<Context>()
            val repository = mockk<AptitudeRepository>()
            
            every { repository.loadQuestions("logical", "blood_relations") } returns listOf(
                QuestionData("Q1", listOf("A", "B"), "A", "E1"),
                QuestionData("Q2", listOf("A", "B"), "B", "E2"),
                QuestionData("Q3", listOf("A", "B"), "A", "E3")
            )

            val viewModel = PracticeQAViewModel(context, repository)
            viewModel.loadQuestions("logical", "blood_relations")

            advanceUntilIdle()

            val state = viewModel.state.value
            state.questions.size shouldBe 3
            state.currentIndex shouldBe 0

            // Test flipCard
            viewModel.flipCard()
            viewModel.state.value.isFlipped shouldBe true

            // Call nextQuestion() twice
            viewModel.nextQuestion()
            viewModel.state.value.currentIndex shouldBe 1
            viewModel.state.value.isFlipped shouldBe false

            viewModel.nextQuestion()
            viewModel.state.value.currentIndex shouldBe 2

            // Call previousQuestion() once
            viewModel.previousQuestion()
            viewModel.state.value.currentIndex shouldBe 1
            viewModel.state.value.isFlipped shouldBe false
        }
    }

    "calling previousQuestion() at currentIndex == 0 is a no-op" {
        runTest {
            val context = mockk<Context>()
            val repository = mockk<AptitudeRepository>()

            every { repository.loadQuestions("logical", "blood_relations") } returns listOf(
                QuestionData("Q1", listOf("A", "B"), "A", "E1")
            )

            val viewModel = PracticeQAViewModel(context, repository)
            viewModel.loadQuestions("logical", "blood_relations")

            advanceUntilIdle()

            viewModel.state.value.currentIndex shouldBe 0
            viewModel.previousQuestion()
            viewModel.state.value.currentIndex shouldBe 0
        }
    }
})
