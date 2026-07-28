package com.example.aiinterviewcoach.ui.recording

import android.Manifest
import android.content.Context
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.Recorder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.LocalStorageHelper
import com.example.aiinterviewcoach.data.local.RecordingRepository
import com.example.aiinterviewcoach.model.RecordingEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// TODO: Replace hardcoded SAMPLE_QUESTION with QuestionBankRepository lookup
//       when the question-bank feature spec is implemented.

// TODO: After saving RecordingEntry, enqueue ML Kit speech-to-text analysis
//       (filler detection, pace analysis) via WorkManager when that spec is implemented.

// TODO: After saving RecordingEntry, enqueue Firebase Storage upload
//       when the Firebase backend spec is implemented.

// TODO: Enable video playback from HistoryFragment item click
//       when the playback spec is implemented.

// TODO: Pass RecordingEntry.filename to scoring engine after transcription
//       when the scoring spec is implemented.

// TODO: Integrate face/eye-contact detection via ML Kit CameraX pipeline
//       when the vision spec is implemented.

@HiltViewModel
class RecorderViewModel @Inject constructor(
    val repository: RecordingRepository,
    val storageHelper: LocalStorageHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    fun setQuestionText(question: String) {
        _uiState.update { it.copy(sampleQuestion = question) }
    }

    private var activeRecording: Recording? = null
    private var pendingEyeContactScore: Int = 0
    private var pendingPostureScore: Int = 0
    private val speechAnalyzer = com.example.aiinterviewcoach.speech.SpeechAnalyzer()
    private var pendingSpeechResult: com.example.aiinterviewcoach.speech.SpeechAnalyzer.SpeechResult? = null

    /**
     * Called by the Fragment's permission launcher result callback.
     * Sets permissionsGranted = true only when BOTH CAMERA and RECORD_AUDIO are granted.
     */
    fun onPermissionsResult(grants: Map<String, Boolean>) {
        val cameraGranted = grants[Manifest.permission.CAMERA] == true
        val audioGranted = grants[Manifest.permission.RECORD_AUDIO] == true
        _uiState.update { it.copy(permissionsGranted = cameraGranted && audioGranted) }
    }

    /**
     * Begins recording using the CameraX VideoCapture API.
     * The Fragment binds the camera and passes in the fully-configured VideoCapture use case
     * along with the pre-created output file and the current Context.
     *
     * Requirements: 3.1, 3.2, 3.3
     */
    @android.annotation.SuppressLint("MissingPermission")
    fun startRecording(
        videoCapture: VideoCapture<Recorder>,
        outputFile: File,
        context: Context
    ) {
        val outputOptions = FileOutputOptions.Builder(outputFile).build()

        speechAnalyzer.startListening(context)

        activeRecording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        // Recording has started
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (event.hasError()) {
                            onRecordingError(event)
                        } else {
                            onRecordingFinalized(event, outputFile)
                        }
                    }
                    else -> Unit
                }
            }

        _uiState.update { it.copy(isRecording = true, errorMessage = null) }
    }

    /**
     * Stops the active recording and immediately marks the UI as saving.
     */
    fun stopRecording(eyeContact: Int, posture: Int) {
        pendingEyeContactScore = eyeContact
        pendingPostureScore = posture
        pendingSpeechResult = speechAnalyzer.stopListening()
        activeRecording?.stop()
        _uiState.update { it.copy(isSaving = true) }
    }

    /**
     * Called when CameraX delivers a successful Finalize event.
     * Persists a RecordingEntry to the Room database and updates UI state.
     */
    private fun onRecordingFinalized(event: VideoRecordEvent.Finalize, outputFile: File) {
        if (event.hasError()) {
            onRecordingError(event)
            return
        }

        val timestampMs = System.currentTimeMillis()
        val speech = pendingSpeechResult ?: com.example.aiinterviewcoach.speech.SpeechAnalyzer.SpeechResult(
            totalWords = 0,
            durationSeconds = 0,
            wordsPerMinute = 0,
            fillerWordCount = 0,
            speechClarityScore = 0,
            speechPaceScore = 0,
            transcribedText = ""
        )

        val speechClarity = speech.speechClarityScore
        val pace = speech.speechPaceScore
        val overall = ((0.30 * speechClarity) + (0.25 * pendingEyeContactScore) + (0.25 * pendingPostureScore) + (0.20 * pace)).toInt()

        val entry = RecordingEntry(
            filename = outputFile.name,
            createdAt = timestampMs,
            overallScore = overall,
            eyeContactScore = pendingEyeContactScore,
            postureScore = pendingPostureScore,
            speechClarityScore = speechClarity,
            paceScore = pace,
            wordsPerMinute = speech.wordsPerMinute,
            fillerWordCount = speech.fillerWordCount,
            durationSeconds = speech.durationSeconds
        )

        viewModelScope.launch {
            repository.insert(entry)
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isSaving = false,
                    savedFilename = outputFile.name,
                    errorMessage = null
                )
            }
        }

        activeRecording = null
    }

    fun clearSavedFilename() {
        _uiState.update { it.copy(savedFilename = null) }
    }

    /**
     * Called when CameraX reports an error during or after recording.
     * Clears all recording flags and surfaces an error message to the UI.
     *
     * Requirements: 3.3, 3.7, 4.5
     */
    private fun onRecordingError(event: VideoRecordEvent) {
        val message = when {
            event is VideoRecordEvent.Finalize && event.hasError() ->
                "Recording failed (error ${event.error})"
            else -> "An unknown recording error occurred"
        }

        _uiState.update {
            it.copy(
                isRecording = false,
                isSaving = false,
                errorMessage = message,
                savedFilename = null
            )
        }

        activeRecording = null
    }
}
