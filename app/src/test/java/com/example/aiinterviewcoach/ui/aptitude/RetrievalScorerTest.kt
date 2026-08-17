package com.example.aiinterviewcoach.ui.aptitude

import com.example.aiinterviewcoach.data.chat.RetrievalScorer
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThan

class RetrievalScorerTest : StringSpec({

    "score sections ranks relevant sections first" {
        val sections = listOf(
            MarkdownSection("Clock Introduction", "A clock has two hands: an hour hand and a minute hand. The angle between hands is important."),
            MarkdownSection("Blood Relations Basics", "Understanding family trees, maternal and paternal relationships. Father, mother, sister, brother."),
            MarkdownSection("Calendar Formulas", "Finding the day of the week for a given date, leap year calculations, odd days concept.")
        )

        // Query about clocks
        val resultsClock = RetrievalScorer.scoreSections(sections, "What is the angle between the hour and minute hands of a clock?")
        resultsClock.first().first.title shouldBe "Clock Introduction"
        (resultsClock.first().second > 0.0) shouldBe true

        // Query about calendar
        val resultsCalendar = RetrievalScorer.scoreSections(sections, "How do you calculate leap years and odd days in calendar?")
        resultsCalendar.first().first.title shouldBe "Calendar Formulas"
        (resultsCalendar.first().second > 0.0) shouldBe true

        // Query about family
        val resultsRelations = RetrievalScorer.scoreSections(sections, "Who is my mother's brother's daughter?")
        resultsRelations.first().first.title shouldBe "Blood Relations Basics"
        (resultsRelations.first().second > 0.0) shouldBe true
    }
})
