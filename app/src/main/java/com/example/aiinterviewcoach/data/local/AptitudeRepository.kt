package com.example.aiinterviewcoach.data.local

import android.content.Context
import com.example.aiinterviewcoach.model.AptitudeProgress
import com.example.aiinterviewcoach.model.QuestionData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

data class AptitudeTopic(
    val id: String,          // e.g. "percentage"
    val name: String,        // e.g. "Percentage"
    val fileName: String,    // e.g. "percentage.md"
    val category: String     // e.g. "quantitative"
)

@Singleton
class AptitudeRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: AptitudeDao,
    private val appPrefs: AppPrefs
) {
    fun getAllProgress(): Flow<List<AptitudeProgress>> = dao.getAllProgressFlow()

    suspend fun markTopicCompleted(category: String, topicId: String) {
        val progress = AptitudeProgress(
            topicId = "$category/$topicId",
            category = category,
            isCompleted = true,
            lastOpenedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdateProgress(progress)
        appPrefs.addXp(5) // Study completion +5 XP
    }

    suspend fun recordTopicOpened(category: String, topicId: String) {
        val existing = dao.getProgressForTopic("$category/$topicId")
        val progress = AptitudeProgress(
            topicId = "$category/$topicId",
            category = category,
            isCompleted = existing?.isCompleted ?: false,
            lastOpenedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdateProgress(progress)
        appPrefs.recordActivity() // Activity streak tick
    }

    fun getTopicsForCategory(category: String): List<AptitudeTopic> {
        val topics = mutableListOf<AptitudeTopic>()
        try {
            val assetPath = "prepare/$category"
            val files = context.assets.list(assetPath) ?: emptyArray()
            for (file in files) {
                if (file.endsWith(".md")) {
                    val topicId = file.substringBeforeLast(".")
                    val topicName = formatTopicName(topicId)
                    topics.add(
                        AptitudeTopic(
                            id = topicId,
                            name = topicName,
                            fileName = file,
                            category = category
                        )
                    )
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return topics.sortedBy { it.name }
    }

    private fun formatTopicName(id: String): String {
        // Special manual override adjustments for nicer titles if needed
        return when (id.lowercase()) {
            "verbal_abilty" -> "Verbal Ability"
            "synonyms_antonyms" -> "Synonyms & Antonyms"
            "time_work" -> "Time & Work"
            "compound_intrest" -> "Compound Interest"
            "simple_intrest" -> "Simple Interest"
            "mixture_alligations" -> "Mixture & Alligations"
            "para_jumbles" -> "Para Jumbles"
            "satements_conclusion" -> "Statements & Conclusion"
            else -> {
                id.split("_").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
        }
    }

    fun getStreakCount(): Int = appPrefs.getStreakCount()
    fun getTotalXp(): Int = appPrefs.getTotalXp()
    fun awardPracticeXp() = appPrefs.addXp(10) // Practice set completion +10 XP

    fun loadQuestions(category: String, topicId: String): List<QuestionData> {
        val questionsList = mutableListOf<QuestionData>()
        if (category.lowercase() == "mixed") {
            val categories = listOf("quantitative", "logical", "verbal")
            for (cat in categories) {
                val topics = getTopicsForCategory(cat)
                for (topic in topics) {
                    questionsList.addAll(loadQuestionsForSingleTopic(cat, topic.id))
                }
            }
            questionsList.shuffle()
        } else {
            if (topicId.isEmpty() || topicId.lowercase() == "all") {
                val topics = getTopicsForCategory(category)
                for (topic in topics) {
                    questionsList.addAll(loadQuestionsForSingleTopic(category, topic.id))
                }
                questionsList.shuffle()
            } else {
                questionsList.addAll(loadQuestionsForSingleTopic(category, topicId))
            }
        }
        return questionsList
    }

    private fun generateOptionsForQuestion(answer: String): List<String> {
        if (answer.isEmpty()) {
            return listOf("Option A", "Option B", "Option C", "Option D")
        }

        val options = mutableSetOf<String>()
        options.add(answer)

        val cleanAnswer = answer.replace(",", "")
        val numberRegex = """\d+""".toRegex()
        val match = numberRegex.find(cleanAnswer)
        if (match != null) {
            val originalNumStr = match.value
            val num = originalNumStr.toLongOrNull()
            if (num != null && num > 0) {
                val variations = mutableListOf<Long>()
                val scales = listOf(0.8, 1.2, 0.9, 1.1, 1.5, 0.5)
                for (scale in scales) {
                    val candidate = (num * scale).toLong()
                    if (candidate > 0 && candidate != num) {
                        variations.add(candidate)
                    }
                }
                val offsets = listOf(10, -10, 5, -5, 20, -20, 1, 2, -1, -2)
                for (offset in offsets) {
                    val candidate = num + offset
                    if (candidate > 0 && candidate != num) {
                        variations.add(candidate)
                    }
                }
                for (v in variations.distinct()) {
                    val candidate = cleanAnswer.replace(originalNumStr, v.toString())
                    options.add(candidate)
                    if (options.size >= 4) break
                }
            }
        }

        val fallbacks = listOf("None of the above", "Cannot be determined", "Data insufficient", "Cannot be calculated")
        for (fallback in fallbacks) {
            if (options.size >= 4) break
            options.add(fallback)
        }

        var letter = 'A'
        while (options.size < 4) {
            options.add("Option $letter")
            letter++
        }

        return options.toList().shuffled()
    }

    private fun loadQuestionsForSingleTopic(category: String, topicId: String): List<QuestionData> {
        val list = mutableListOf<QuestionData>()
        val jsonFileName = "prepare/$category/${topicId}_questions.json"
        try {
            val jsonStr = context.assets.open(jsonFileName).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val question = obj.optString("question", "")
                val answer = obj.optString("answer", "")
                val explanation = obj.optString("explanation", "")
                
                if (question.trim().isEmpty() || answer.trim().isEmpty()) {
                    continue
                }
                
                val optionsArray = obj.optJSONArray("options")
                val options = mutableListOf<String>()
                if (optionsArray != null) {
                    for (j in 0 until optionsArray.length()) {
                        val opt = optionsArray.optString(j, "")
                        if (opt.trim().isNotEmpty()) {
                            options.add(opt)
                        }
                    }
                }
                if (options.isEmpty()) {
                    options.addAll(generateOptionsForQuestion(answer))
                }
                if (options.size < 2) {
                    continue
                }
                list.add(QuestionData(question, options, answer, explanation))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
