package com.example.aiinterviewcoach.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class SpeechAnalyzer {

    data class SpeechResult(
        val totalWords: Int,
        val durationSeconds: Long,
        val wordsPerMinute: Int,
        val fillerWordCount: Int,
        val speechClarityScore: Int,
        val speechPaceScore: Int,
        val transcribedText: String
    )

    private var speechRecognizer: SpeechRecognizer? = null
    private var startTimeMs: Long = 0L
    private val transcribedBuffer = StringBuilder()
    private var isListening = false

    private val fillerWordsSet = setOf(
        "um", "uh", "like", "you know", "actually", "basically", "ah", "er", "so", "mean", "right"
    )

    fun startListening(context: Context, onPartialResult: ((String) -> Unit)? = null) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w("SpeechAnalyzer", "Speech recognition not available on this device")
            return
        }

        startTimeMs = System.currentTimeMillis()
        transcribedBuffer.clear()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        // Automatically restart if session is ongoing (for continuous listening during long interview answers)
                        if (isListening) {
                            try {
                                startListeningInternal()
                            } catch (e: Exception) {
                                Log.e("SpeechAnalyzer", "Error restarting recognizer", e)
                            }
                        }
                    }

                    override fun onError(error: Int) {
                        Log.d("SpeechAnalyzer", "SpeechRecognizer error: $error")
                        if (isListening && error != SpeechRecognizer.ERROR_CLIENT) {
                            try {
                                startListeningInternal()
                            } catch (e: Exception) {
                                Log.e("SpeechAnalyzer", "Error restarting recognizer after error", e)
                            }
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0]
                            if (transcribedBuffer.isNotEmpty()) {
                                transcribedBuffer.append(" ")
                            }
                            transcribedBuffer.append(text)
                            onPartialResult?.invoke(transcribedBuffer.toString())
                        }
                        if (isListening) {
                            startListeningInternal()
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            onPartialResult?.invoke(transcribedBuffer.toString() + " " + matches[0])
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            isListening = true
            startListeningInternal()
        } catch (e: Exception) {
            Log.e("SpeechAnalyzer", "Failed to start SpeechRecognizer", e)
        }
    }

    private fun startListeningInternal() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening(): SpeechResult {
        isListening = false
        val durationMs = Math.max(1000L, System.currentTimeMillis() - startTimeMs)
        val durationSec = durationMs / 1000L

        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.e("SpeechAnalyzer", "Error stopping SpeechRecognizer", e)
        }

        val fullText = transcribedBuffer.toString().trim()
        val words = if (fullText.isEmpty()) emptyList() else fullText.lowercase(Locale.ROOT).split("\\s+".toRegex())
        val totalWords = words.size

        // Calculate Words Per Minute (WPM)
        val minutes = Math.max(1.0 / 60.0, durationSec / 60.0)
        val wpm = (totalWords / minutes).toInt()

        // Count filler words
        var fillerCount = 0
        for (word in words) {
            val cleanWord = word.replace("[^a-zA-Z]".toRegex(), "")
            if (fillerWordsSet.contains(cleanWord)) {
                fillerCount++
            }
        }

        // Calculate Speech Pace Score (Ideal range: 120 - 160 WPM)
        val paceScore = calculatePaceScore(wpm, totalWords)

        // Calculate Speech Clarity Score (Based on filler word density & word count)
        val clarityScore = calculateClarityScore(totalWords, fillerCount)

        return SpeechResult(
            totalWords = totalWords,
            durationSeconds = durationSec,
            wordsPerMinute = wpm,
            fillerWordCount = fillerCount,
            speechClarityScore = clarityScore,
            speechPaceScore = paceScore,
            transcribedText = fullText
        )
    }

    companion object {
        fun calculatePaceScore(wpm: Int, totalWords: Int): Int {
            if (totalWords == 0) return 0
            if (totalWords < 6) return Math.min(50, totalWords * 8) // Penalize very low word count
            return when {
                wpm in 120..165 -> Math.min(100, 95 + ((wpm % 5))) // optimal pace (95-100%)
                wpm in 100..119 -> 80 + ((wpm - 100) * 15 / 20) // slightly slow (80-94%)
                wpm in 166..185 -> 80 + ((185 - wpm) * 15 / 20) // slightly fast (80-94%)
                wpm in 70..99 -> 65 + ((wpm - 70) * 15 / 30) // slow (65-79%)
                wpm in 186..215 -> 65 + ((215 - wpm) * 15 / 30) // fast (65-79%)
                wpm < 70 -> Math.max(20, 30 + (wpm / 3))
                else -> Math.max(20, 60 - ((wpm - 215) / 3))
            }
        }

        fun calculateClarityScore(totalWords: Int, fillerCount: Int): Int {
            if (totalWords == 0) return 0
            if (totalWords < 6) return Math.min(50, totalWords * 8) // Penalize insufficient word count
            val fillerPercentage = (fillerCount.toDouble() / totalWords.toDouble()) * 100.0
            return when {
                fillerPercentage == 0.0 -> 96
                fillerPercentage <= 3.0 -> 92
                fillerPercentage <= 6.0 -> 84
                fillerPercentage <= 10.0 -> 75
                fillerPercentage <= 15.0 -> 64
                else -> Math.max(20, (60 - (fillerPercentage - 15) * 2).toInt())
            }
        }
    }
}
