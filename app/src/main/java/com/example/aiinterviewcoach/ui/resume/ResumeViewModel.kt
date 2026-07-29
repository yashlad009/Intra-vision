package com.example.aiinterviewcoach.ui.resume

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiinterviewcoach.data.local.ResumeRepository
import com.example.aiinterviewcoach.model.ResumeEntity
import com.example.aiinterviewcoach.scoring.ResumeHeuristicsEngine
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ResumeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resumeRepository: ResumeRepository
) : ViewModel() {

    val latestResume: StateFlow<ResumeEntity?> = resumeRepository.latestResume
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _navigateToAnalysis = MutableSharedFlow<Unit>()
    val navigateToAnalysis: SharedFlow<Unit> = _navigateToAnalysis.asSharedFlow()

    init {
        // Initialize PdfBox Android resource loader once
        try {
            PDFBoxResourceLoader.init(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun processPdfUri(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _isExtracting.value = true
            _errorMessage.value = null

            val result = withContext(Dispatchers.IO) {
                try {
                    val contentResolver = context.contentResolver
                    val extractedText = contentResolver.openInputStream(uri)?.use { inputStream ->
                        val document = PDDocument.load(inputStream)
                        val stripper = PDFTextStripper()
                        val text = stripper.getText(document)
                        document.close()
                        text
                    } ?: ""

                    if (extractedText.isBlank()) {
                        Result.failure<Unit>(
                            Exception("Could not extract readable text from PDF. Ensure it is not an image-only scan.")
                        )
                    } else {
                        val analysisResult = ResumeHeuristicsEngine.analyze(extractedText)
                        val jsonStr = analysisResult.toJson()
                        resumeRepository.saveResume(fileName, extractedText, jsonStr)
                        Result.success(Unit)
                    }
                } catch (e: Exception) {
                    Result.failure<Unit>(e)
                }
            }

            _isExtracting.value = false

            result.onSuccess {
                _navigateToAnalysis.emit(Unit)
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Failed to process PDF resume."
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

