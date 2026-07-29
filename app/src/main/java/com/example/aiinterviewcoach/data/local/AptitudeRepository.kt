package com.example.aiinterviewcoach.data.local

import android.content.Context
import com.example.aiinterviewcoach.model.AptitudeProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
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
}
