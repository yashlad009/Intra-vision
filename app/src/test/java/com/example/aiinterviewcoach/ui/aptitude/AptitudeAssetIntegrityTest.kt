package com.example.aiinterviewcoach.ui.aptitude

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.json.JSONArray
import java.io.File

class AptitudeAssetIntegrityTest : StringSpec({

    "verify all markdown files have valid corresponding json questions files" {
        val prepareDir = File("src/main/assets/prepare")
        prepareDir.exists() shouldBe true

        val categories = listOf("logical", "quantitative", "verbal")
        for (category in categories) {
            val catDir = File(prepareDir, category)
            catDir.exists() shouldBe true

            val mdFiles = catDir.listFiles { _, name -> name.endsWith(".md") } ?: emptyArray()
            mdFiles.isNotEmpty() shouldBe true

            for (mdFile in mdFiles) {
                val topicId = mdFile.name.substringBeforeLast(".")
                val jsonFile = File(catDir, "${topicId}_questions.json")
                
                // Assert both exist
                jsonFile.exists() shouldBe true
                
                // Assert json file parses successfully
                val jsonContent = jsonFile.readText(Charsets.UTF_8)
                try {
                    val jsonArray = JSONArray(jsonContent)
                    // Check if format contains question when not empty
                    if (jsonArray.length() > 0) {
                        val firstObj = jsonArray.getJSONObject(0)
                        firstObj.has("question") shouldBe true
                    }
                } catch (e: Exception) {
                    throw AssertionError("File ${jsonFile.path} failed to parse as valid JSON array: ${e.message}")
                }
            }
        }
    }
})
