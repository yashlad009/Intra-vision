package com.example.aiinterviewcoach.ui.aptitude

import android.content.Context
import android.content.res.AssetManager
import com.example.aiinterviewcoach.data.local.AptitudeDao
import com.example.aiinterviewcoach.data.local.AppPrefs
import com.example.aiinterviewcoach.data.local.AptitudeRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream

class AptitudeRepositoryTest : StringSpec({
    "loadQuestions with specific topicId returns only that topic questions count" {
        val context = mockk<Context>()
        val dao = mockk<AptitudeDao>()
        val appPrefs = mockk<AppPrefs>()
        val assetManager = mockk<AssetManager>()

        every { context.assets } returns assetManager
        every { assetManager.list("prepare/quantitative") } returns arrayOf("algebra.md", "average.md")
        
        val algebraJson = """[{"question":"Q1","options":[],"answer":"A1","explanation":"E1"}]"""
        every { assetManager.open("prepare/quantitative/algebra_questions.json") } returns ByteArrayInputStream(algebraJson.toByteArray())

        val repository = AptitudeRepository(context, dao, appPrefs)
        val questions = repository.loadQuestions("quantitative", "algebra")
        questions.size shouldBe 1
        questions[0].question shouldBe "Q1"
    }

    "loadQuestions with all returns all category questions count" {
        val context = mockk<Context>()
        val dao = mockk<AptitudeDao>()
        val appPrefs = mockk<AppPrefs>()
        val assetManager = mockk<AssetManager>()

        every { context.assets } returns assetManager
        every { assetManager.list("prepare/quantitative") } returns arrayOf("algebra.md", "average.md")
        
        val algebraJson = """[{"question":"Q1","options":[],"answer":"A1","explanation":"E1"}]"""
        val averageJson = """[{"question":"Q2","options":[],"answer":"A2","explanation":"E2"}]"""
        every { assetManager.open("prepare/quantitative/algebra_questions.json") } returns ByteArrayInputStream(algebraJson.toByteArray())
        every { assetManager.open("prepare/quantitative/average_questions.json") } returns ByteArrayInputStream(averageJson.toByteArray())

        val repository = AptitudeRepository(context, dao, appPrefs)
        val questions = repository.loadQuestions("quantitative", "all")
        questions.size shouldBe 2
    }

    "loadQuestions filters out questions with empty answers" {
        val context = mockk<Context>()
        val dao = mockk<AptitudeDao>()
        val appPrefs = mockk<AppPrefs>()
        val assetManager = mockk<AssetManager>()

        every { context.assets } returns assetManager
        
        val algebraJson = """[
            {"question":"Q1","options":[],"answer":"","explanation":"E1"},
            {"question":"Q2","options":[],"answer":"A2","explanation":"E2"}
        ]"""
        every { assetManager.open("prepare/quantitative/algebra_questions.json") } returns ByteArrayInputStream(algebraJson.toByteArray())

        val repository = AptitudeRepository(context, dao, appPrefs)
        val questions = repository.loadQuestions("quantitative", "algebra")
        questions.size shouldBe 1
        questions[0].question shouldBe "Q2"
    }

    "loadQuestions generates options if options list is empty" {
        val context = mockk<Context>()
        val dao = mockk<AptitudeDao>()
        val appPrefs = mockk<AppPrefs>()
        val assetManager = mockk<AssetManager>()

        every { context.assets } returns assetManager
        
        val algebraJson = """[{"question":"Q1","options":[],"answer":"10","explanation":"E1"}]"""
        every { assetManager.open("prepare/quantitative/algebra_questions.json") } returns ByteArrayInputStream(algebraJson.toByteArray())

        val repository = AptitudeRepository(context, dao, appPrefs)
        val questions = repository.loadQuestions("quantitative", "algebra")
        questions.size shouldBe 1
        val q = questions[0]
        q.options.size shouldBe 4
        q.options.contains("10") shouldBe true
    }
})
