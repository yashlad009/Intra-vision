package com.example.aiinterviewcoach.data.chat

import com.example.aiinterviewcoach.BuildConfig
import com.example.aiinterviewcoach.ui.aptitude.MarkdownSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyChatRepository @Inject constructor() {

    suspend fun getChatResponse(
        sections: List<MarkdownSection>,
        userQuestion: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey.startsWith("AIzaSyDummy")) {
                return@withContext Result.failure(Exception("Gemini API key is not configured or is placeholder."))
            }

            // Build context from sections
            val contextBuilder = StringBuilder()
            sections.forEach { sec ->
                contextBuilder.append("Section: ").append(sec.title).append("\n")
                contextBuilder.append(sec.content).append("\n\n")
            }

            val prompt = """
                You are a helpful AI study assistant. Answer the user's question about the topic using only the provided notes sections.
                If the provided notes do not contain the answer, you must state: "This isn't covered in these notes".
                Do not make up facts or use external knowledge. Keep your answers concise, clear, and grounded.

                ---
                PROVIDED STUDY NOTES:
                ${contextBuilder.toString()}
                ---

                User Question: $userQuestion
                Answer:
            """.trimIndent()

            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            // Request body JSON
            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // Write output
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseStr = BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.readText()
                }
                
                // Parse response JSON
                val responseObj = JSONObject(responseStr)
                val candidates = responseObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val replyText = parts.getJSONObject(0).optString("text", "")
                            return@withContext Result.success(replyText.trim())
                        }
                    }
                }
                Result.failure(Exception("Failed to parse response content."))
            } else {
                val errorStream = connection.errorStream
                val errorText = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                } else {
                    "No error body"
                }
                Result.failure(Exception("Gemini API call failed with code $responseCode: $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
