package com.example.aiinterviewcoach.ui.recording

data class RecordingUiState(
    val permissionsGranted: Boolean = false,
    val isRecording: Boolean = false,
    val isSaving: Boolean = false,
    val savedFilename: String? = null,      // non-null triggers Snackbar
    val errorMessage: String? = null,       // non-null triggers error UI
    val sampleQuestion: String = SAMPLE_QUESTION
) {
    companion object {
        const val SAMPLE_QUESTION = "Tell me about yourself."
        // TODO: replace with question bank lookup when Requirement 'Question bank' is implemented
    }
}
