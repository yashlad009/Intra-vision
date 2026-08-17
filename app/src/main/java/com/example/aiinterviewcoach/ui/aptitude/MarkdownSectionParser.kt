package com.example.aiinterviewcoach.ui.aptitude

data class MarkdownSection(
    val title: String,
    val content: String
)

object MarkdownSectionParser {

    fun parse(markdown: String): List<MarkdownSection> {
        val sections = mutableListOf<MarkdownSection>()
        val lines = markdown.split("\n")
        var currentTitle = "Introduction"
        val currentContent = StringBuilder()

        for (line in lines) {
            // Match H1, H2, or H3 headings
            if (line.startsWith("# ") || line.startsWith("## ") || line.startsWith("### ")) {
                if (currentContent.isNotEmpty()) {
                    sections.add(MarkdownSection(currentTitle, currentContent.toString().trim()))
                    currentContent.clear()
                }
                currentTitle = line.substring(line.indexOf(' ') + 1).trim()
            } else {
                currentContent.append(line).append("\n")
            }
        }
        if (currentContent.isNotEmpty()) {
            sections.add(MarkdownSection(currentTitle, currentContent.toString().trim()))
        }
        return sections
    }

    fun groupSections(sections: List<MarkdownSection>): Triple<List<MarkdownSection>, List<MarkdownSection>, List<MarkdownSection>> {
        val learn = mutableListOf<MarkdownSection>()
        val practice = mutableListOf<MarkdownSection>()
        val revise = mutableListOf<MarkdownSection>()

        for (section in sections) {
            val title = section.title.lowercase()
            when {
                // Practice Keywords (MCQs, Solved examples, exercises, company patterns)
                title.contains("solved") || 
                title.contains("practice") ||
                title.contains("questions") ||
                title.contains("rapid fire") ||
                title.contains("concept check") ||
                title.contains("beginner") ||
                title.contains("intermediate") ||
                title.contains("advanced") ||
                title.contains("placement") ||
                title.contains("tcs") ||
                title.contains("infosys") ||
                title.contains("accenture") ||
                title.contains("capgemini") ||
                title.contains("cognizant") ||
                title.contains("deloitte") ||
                title.contains("wipro") ||
                title.contains("exercise") -> {
                    practice.add(section)
                }
                // Revise Keywords (summaries, formulas, key takeaways, common mistakes, congrats)
                title.contains("revision") || 
                title.contains("summary") || 
                title.contains("takeaways") || 
                title.contains("congratulations") || 
                title.contains("mistakes") || 
                title.contains("formula") ||
                title.contains("cheat sheet") ||
                title.contains("assessment") ||
                title.contains("pattern") -> {
                    revise.add(section)
                }
                // Fallback / Learn
                else -> {
                    if (practice.isNotEmpty() && revise.isEmpty()) {
                        practice.add(section)
                    } else if (revise.isNotEmpty()) {
                        revise.add(section)
                    } else {
                        learn.add(section)
                    }
                }
            }
        }

        // Fallback: If learn or revise is empty, balance it
        if (learn.isEmpty() && sections.isNotEmpty()) {
            learn.add(sections.first())
        }

        return Triple(learn, practice, revise)
    }
}
