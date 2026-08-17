package com.example.aiinterviewcoach.ui.aptitude

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.ints.shouldBeGreaterThan
import java.io.File

class MarkdownSectionParserTest : StringSpec({

    "parse real blood_relations.md and assert structure" {
        val file = File("src/main/assets/prepare/logical/blood_relations.md")
        file.exists() shouldBe true

        val content = file.readText(Charsets.UTF_8)
        val sections = MarkdownSectionParser.parse(content)

        sections.size shouldBeGreaterThan 3
        
        // Assert some specific headings exist
        sections.any { it.title.contains("Introduction") } shouldBe true

        val (learn, practice, revise) = MarkdownSectionParser.groupSections(sections)

        learn.isNotEmpty() shouldBe true
        practice.isNotEmpty() shouldBe true
        revise.isNotEmpty() shouldBe true
    }

    "parse real clock.md and assert structure" {
        val file = File("src/main/assets/prepare/logical/clock.md")
        file.exists() shouldBe true

        val content = file.readText(Charsets.UTF_8)
        val sections = MarkdownSectionParser.parse(content)

        sections.size shouldBeGreaterThan 3

        val (learn, practice, revise) = MarkdownSectionParser.groupSections(sections)

        learn.isNotEmpty() shouldBe true
        practice.isNotEmpty() shouldBe true
        revise.isNotEmpty() shouldBe true
    }

    "parse real idioms.md and assert structure" {
        val file = File("src/main/assets/prepare/verbal/idioms.md")
        file.exists() shouldBe true

        val content = file.readText(Charsets.UTF_8)
        val sections = MarkdownSectionParser.parse(content)

        sections.size shouldBeGreaterThan 3

        val (learn, practice, revise) = MarkdownSectionParser.groupSections(sections)

        learn.isNotEmpty() shouldBe true
        practice.isNotEmpty() shouldBe true
        revise.isNotEmpty() shouldBe true
    }
})
