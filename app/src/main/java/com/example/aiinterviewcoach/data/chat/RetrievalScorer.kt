package com.example.aiinterviewcoach.data.chat

import com.example.aiinterviewcoach.ui.aptitude.MarkdownSection
import kotlin.math.log10

object RetrievalScorer {

    private val STOPWORDS = setOf(
        "a", "an", "the", "and", "or", "but", "is", "are", "was", "were", "of", "in", 
        "on", "at", "to", "for", "with", "by", "about", "what", "how", "why", "where", 
        "when", "who", "which", "this", "that", "these", "those", "i", "you", "he", "she", 
        "it", "they", "we", "us", "them", "my", "your", "his", "her", "their", "our", "me"
    )

    fun scoreSections(sections: List<MarkdownSection>, query: String): List<Pair<MarkdownSection, Double>> {
        val queryTerms = tokenize(query).filter { it !in STOPWORDS }.toSet()
        if (queryTerms.isEmpty() || sections.isEmpty()) {
            return sections.map { it to 0.0 }
        }

        // Calculate document frequency (DF) for each query term in this set of sections
        val df = mutableMapOf<String, Int>()
        for (term in queryTerms) {
            var count = 0
            for (sec in sections) {
                val secTitleTerms = tokenize(sec.title)
                val secContentTerms = tokenize(sec.content)
                if (term in secTitleTerms || term in secContentTerms) {
                    count++
                }
            }
            df[term] = count
        }

        // Calculate IDF for each query term
        val idf = mutableMapOf<String, Double>()
        val n = sections.size.toDouble()
        for (term in queryTerms) {
            val docFreq = df[term] ?: 0
            // IDF-lite formula
            idf[term] = log10((n + 1.0) / (docFreq + 1.0))
        }

        // Score each section
        val scoredSections = mutableListOf<Pair<MarkdownSection, Double>>()
        for (sec in sections) {
            var score = 0.0
            val secTitleTerms = tokenize(sec.title)
            val secContentTerms = tokenize(sec.content)

            for (term in queryTerms) {
                val termIdf = idf[term] ?: 0.0
                
                // Title matches get weighted 3x
                if (term in secTitleTerms) {
                    score += 3.0 * termIdf
                }
                // Content matches get 1x
                if (term in secContentTerms) {
                    score += 1.0 * termIdf
                }
            }
            scoredSections.add(sec to score)
        }

        // Sort descending by score
        return scoredSections.sortedByDescending { it.second }
    }

    private fun tokenize(text: String): Set<String> {
        return text.lowercase()
            .replace("'s", "")
            .replace(Regex("[^a-zA-Z0-9\\s]"), "") // Strip punctuation
            .split(Regex("\\s+"))                  // Split by whitespace
            .filter { it.isNotEmpty() }
            .toSet()
    }
}
