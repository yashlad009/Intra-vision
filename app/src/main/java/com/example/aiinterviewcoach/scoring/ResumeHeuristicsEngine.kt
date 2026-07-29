package com.example.aiinterviewcoach.scoring

import org.json.JSONArray
import org.json.JSONObject

data class CheckResult(
    val title: String,
    val isPassed: Boolean,
    val explanation: String
)

data class RecommendationItem(
    val category: String,
    val title: String,
    val description: String
)

data class ResumeAnalysisResult(
    val totalScore: Int,
    val checks: List<CheckResult>,
    val recommendations: List<RecommendationItem>
) {
    fun toJson(): String {
        val json = JSONObject()
        json.put("totalScore", totalScore)

        val checksArray = JSONArray()
        checks.forEach { check ->
            val cObj = JSONObject()
            cObj.put("title", check.title)
            cObj.put("isPassed", check.isPassed)
            cObj.put("explanation", check.explanation)
            checksArray.put(cObj)
        }
        json.put("checks", checksArray)

        val recsArray = JSONArray()
        recommendations.forEach { rec ->
            val rObj = JSONObject()
            rObj.put("category", rec.category)
            rObj.put("title", rec.title)
            rObj.put("description", rec.description)
            recsArray.put(rObj)
        }
        json.put("recommendations", recsArray)

        return json.toString()
    }

    companion object {
        fun fromJson(jsonStr: String): ResumeAnalysisResult {
            if (jsonStr.isBlank()) {
                return ResumeAnalysisResult(0, emptyList(), emptyList())
            }
            try {
                val json = JSONObject(jsonStr)
                val totalScore = json.optInt("totalScore", 0)

                val checksList = mutableListOf<CheckResult>()
                val checksArray = json.optJSONArray("checks")
                if (checksArray != null) {
                    for (i in 0 until checksArray.length()) {
                        val obj = checksArray.getJSONObject(i)
                        checksList.add(
                            CheckResult(
                                title = obj.optString("title", ""),
                                isPassed = obj.optBoolean("isPassed", false),
                                explanation = obj.optString("explanation", "")
                            )
                        )
                    }
                }

                val recsList = mutableListOf<RecommendationItem>()
                val recsArray = json.optJSONArray("recommendations")
                if (recsArray != null) {
                    for (i in 0 until recsArray.length()) {
                        val obj = recsArray.getJSONObject(i)
                        recsList.add(
                            RecommendationItem(
                                category = obj.optString("category", ""),
                                title = obj.optString("title", ""),
                                description = obj.optString("description", "")
                            )
                        )
                    }
                }

                return ResumeAnalysisResult(totalScore, checksList, recsList)
            } catch (e: Exception) {
                return ResumeAnalysisResult(0, emptyList(), emptyList())
            }
        }
    }
}

object ResumeHeuristicsEngine {

    private val ACTION_VERBS = listOf(
        "Built", "Led", "Designed", "Implemented", "Optimized",
        "Developed", "Created", "Engineered", "Architected", "Spearheaded",
        "Managed", "Formulated", "Established", "Executed", "Improved",
        "Refactored", "Deployed", "Automated", "Delivered", "Initiated",
        "Orchestrated", "Pioneered", "Resolved", "Transformed", "Streamlined"
    )

    fun analyze(extractedText: String): ResumeAnalysisResult {
        val checks = mutableListOf<CheckResult>()
        var passedCount = 0

        // 1. Section Detection
        val sectionResult = checkSections(extractedText)
        checks.add(sectionResult)
        if (sectionResult.isPassed) passedCount++

        // 2. Contact Info Check
        val contactResult = checkContactInfo(extractedText)
        checks.add(contactResult)
        if (contactResult.isPassed) passedCount++

        // 3. Length Check
        val lengthResult = checkLength(extractedText)
        checks.add(lengthResult)
        if (lengthResult.isPassed) passedCount++

        // 4. Action Verb Density
        val actionVerbResult = checkActionVerbs(extractedText)
        checks.add(actionVerbResult)
        if (actionVerbResult.isPassed) passedCount++

        // Calculate score out of 100 based on checks
        val totalScore = (passedCount * 100) / checks.size

        // 5. Skill-to-Practice Mapping
        val recommendations = mapSkillsToPractice(extractedText)

        return ResumeAnalysisResult(
            totalScore = totalScore,
            checks = checks,
            recommendations = recommendations
        )
    }

    private fun checkSections(text: String): CheckResult {
        val sectionPatterns = mapOf(
            "Education" to Regex("(?i)\\b(education|academic background|qualifications)\\b"),
            "Experience" to Regex("(?i)\\b(experience|work experience|employment|work history)\\b"),
            "Skills" to Regex("(?i)\\b(skills|technical skills|key skills|technologies)\\b"),
            "Projects" to Regex("(?i)\\b(projects|academic projects|personal projects)\\b"),
            "Certifications" to Regex("(?i)\\b(certifications|certificates|credentials|courses)\\b")
        )

        val missingSections = mutableListOf<String>()
        for ((sectionName, pattern) in sectionPatterns) {
            if (!pattern.containsMatchIn(text)) {
                missingSections.add(sectionName)
            }
        }

        return if (missingSections.isEmpty()) {
            CheckResult(
                title = "Section Detection",
                isPassed = true,
                explanation = "All 5 core resume sections (Education, Experience, Skills, Projects, Certifications) detected."
            )
        } else {
            CheckResult(
                title = "Section Detection",
                isPassed = false,
                explanation = "Missing section headers: ${missingSections.joinToString(", ")}. Adding clear headers improves readability."
            )
        }
    }

    private fun checkContactInfo(text: String): CheckResult {
        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        val phoneRegex = Regex("(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}|\\b\\d{10}\\b")

        val hasEmail = emailRegex.containsMatchIn(text)
        val hasPhone = phoneRegex.containsMatchIn(text)

        return if (hasEmail && hasPhone) {
            CheckResult(
                title = "Contact Information",
                isPassed = true,
                explanation = "Contact details (Email & 10-digit Phone) detected."
            )
        } else {
            val missing = when {
                !hasEmail && !hasPhone -> "Email and Phone number"
                !hasEmail -> "Email address"
                else -> "10-digit Phone number"
            }
            CheckResult(
                title = "Contact Information",
                isPassed = false,
                explanation = "Missing contact details: $missing."
            )
        }
    }

    private fun checkLength(text: String): CheckResult {
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val wordCount = words.size

        return when {
            wordCount < 200 -> CheckResult(
                title = "Resume Length",
                isPassed = false,
                explanation = "Resume is too sparse ($wordCount words). Aim for 200–1200 words for student/entry roles."
            )
            wordCount > 1200 -> CheckResult(
                title = "Resume Length",
                isPassed = false,
                explanation = "Resume is too long ($wordCount words). Try condensing to 1–2 pages (< 1200 words)."
            )
            else -> CheckResult(
                title = "Resume Length",
                isPassed = true,
                explanation = "Optimal resume length ($wordCount words)."
            )
        }
    }

    private fun checkActionVerbs(text: String): CheckResult {
        val foundVerbs = mutableSetOf<String>()
        for (verb in ACTION_VERBS) {
            val regex = Regex("(?i)\\b$verb\\b")
            if (regex.containsMatchIn(text)) {
                foundVerbs.add(verb)
            }
        }

        val count = foundVerbs.size
        return if (count >= 5) {
            val sample = foundVerbs.take(3).joinToString(", ")
            CheckResult(
                title = "Action Verb Density",
                isPassed = true,
                explanation = "Strong action verb usage ($count verbs found, e.g. $sample)."
            )
        } else {
            CheckResult(
                title = "Action Verb Density",
                isPassed = false,
                explanation = "Low action verb usage ($count found). Use strong action verbs (Built, Designed, Implemented) to highlight achievements."
            )
        }
    }

    private fun mapSkillsToPractice(text: String): List<RecommendationItem> {
        val recommendations = mutableListOf<RecommendationItem>()

        // Technical / Database keywords
        val techKeywords = listOf("dbms", "sql", "database", "mysql", "postgresql", "mongodb", "redis", "java", "kotlin", "python", "c++", "data structures", "algorithms")
        val techCount = techKeywords.count { Regex("(?i)\\b$it\\b").containsMatchIn(text) }

        if (techCount >= 2) {
            recommendations.add(
                RecommendationItem(
                    category = "Technical",
                    title = "Technical Questions",
                    description = "Your resume mentions database or technical work — practice Technical Questions in the Interview tab."
                )
            )
        }

        // Behavioral / Leadership keywords
        val behavioralKeywords = listOf("team", "leadership", "collaborate", "managed", "communicated", "agile", "scrum", "lead", "project")
        val behavioralCount = behavioralKeywords.count { Regex("(?i)\\b$it\\b").containsMatchIn(text) }

        if (behavioralCount >= 2) {
            recommendations.add(
                RecommendationItem(
                    category = "Behavioral",
                    title = "Behavioral (STAR) Questions",
                    description = "Your resume mentions teamwork or project experience — practice Behavioral STAR Questions in the Interview tab."
                )
            )
        }

        // HR / Fit fallback if no specific technical/behavioral triggered
        if (recommendations.isEmpty()) {
            recommendations.add(
                RecommendationItem(
                    category = "HR",
                    title = "HR & General Fit Questions",
                    description = "Practice HR & introductory questions to sharpen your communication skills."
                )
            )
        }

        return recommendations
    }
}
