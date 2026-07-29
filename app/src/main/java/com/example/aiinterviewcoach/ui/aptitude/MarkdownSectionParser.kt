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
            // Match H1 or H2 headings
            if (line.startsWith("# ") || line.startsWith("## ")) {
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
                // Practice Keywords
                title.contains("solved") || 
                title.contains("interview questions") || 
                title.contains("practice") ||
                title.contains("rapid fire") ||
                title.contains("concept check") -> {
                    practice.add(section)
                }
                // Revise Keywords
                title.contains("pattern") || 
                title.contains("revision") || 
                title.contains("assessment") || 
                title.contains("takeaways") || 
                title.contains("summary") || 
                title.contains("congratulations") || 
                title.contains("next") -> {
                    revise.add(section)
                }
                // Fallback / Learn
                else -> {
                    // Check if we are already in practice/revise phase but got a generic heading
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
