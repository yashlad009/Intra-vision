package com.example.aiinterviewcoach.ui.recording

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import com.example.aiinterviewcoach.vision.FacePoseAnalyzer
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentRecordingBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecordingFragment : Fragment() {

    private val viewModel: RecorderViewModel by viewModels()
    private val args: RecordingFragmentArgs by navArgs()
    private var _binding: FragmentRecordingBinding? = null
    private val binding get() = _binding!!
    private var videoCapture: VideoCapture<Recorder>? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var visionAnalyzer: FacePoseAnalyzer? = null

    // ── Task 9.2: Permission launcher ──────────────────────────────────────────
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        viewModel.onPermissionsResult(grants)
        if (grants[Manifest.permission.CAMERA] == true &&
            grants[Manifest.permission.RECORD_AUDIO] == true
        ) {
            startCamera()
        }
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setQuestionText(args.questionText)
        checkAndRequestPermissions()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { renderState(it) }
        }
        setupButtons()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Task 9.2: Permission gating & CameraX binding ─────────────────────────

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) ==
                PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            viewModel.onPermissionsResult(
                mapOf(
                    Manifest.permission.CAMERA to true,
                    Manifest.permission.RECORD_AUDIO to true
                )
            )
            startCamera()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            // Setup ImageAnalysis for ML Kit Real-time Face & Pose Analysis
            visionAnalyzer = FacePoseAnalyzer { result ->
                activity?.runOnUiThread {
                    if (_binding != null) {
                        binding.overlayView.updateOverlay(
                            rawFaceBox = result.faceBox,
                            eyeContact = result.eyeContactMaintained,
                            postureFeedback = result.postureFeedback,
                            rawLandmarks = result.poseLandmarks,
                            imageWidth = result.imageWidth,
                            imageHeight = result.imageHeight,
                            rotationDegrees = result.rotationDegrees
                        )
                    }
                }
            }

            val analysisExecutor = ContextCompat.getMainExecutor(requireContext())
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(analysisExecutor, visionAnalyzer!!)
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    videoCapture!!,
                    imageAnalysis!!
                )
            } catch (e: Exception) {
                // Camera bind failure — surface error to user via Snackbar (Requirement 3.3)
                Snackbar.make(
                    binding.root,
                    "Camera unavailable: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // ── Task 9.3: Button wiring ────────────────────────────────────────────────

    private fun setupButtons() {
        // Record button → start recording (Requirement 3.1, 3.2)
        binding.btnRecord.setOnClickListener {
            val vc = videoCapture ?: return@setOnClickListener
            val outputFile = viewModel.storageHelper.createOutputFile()
            visionAnalyzer?.startTracking() // Start face/posture metrics tracking
            viewModel.startRecording(vc, outputFile, requireContext())
        }

        // Stop button → stop recording (Requirement 4.1, 4.2)
        binding.btnStop.setOnClickListener {
            val summary = visionAnalyzer?.stopTracking()
            val eyeScore = summary?.eyeContactPercentage ?: 100
            val postureScore = summary?.posturePercentage ?: 100

            viewModel.stopRecording(eyeScore, postureScore)

            if (summary != null) {
                android.util.Log.d(
                    "InterviewVision",
                    "Session Vision Metrics Summary: Eye Contact = $eyeScore%, Posture = $postureScore%, Total Frames = ${summary.totalFramesAnalyzed}"
                )
            }
            binding.overlayView.clear() // Clear skeletal drawings when stopped
        }

        // History navigation via long-press on Record button (Requirement 5.1)
        // Uses the action defined in nav_recording.xml
        binding.btnRecord.setOnLongClickListener {
            findNavController().navigate(R.id.action_recordingFragment_to_historyFragment)
            true
        }
    }

    // ── Task 9.3: State rendering ──────────────────────────────────────────────

    private fun renderState(state: RecordingUiState) {
        // Sample question always visible (Requirements 1.1, 1.2, 1.3)
        binding.tvQuestion.text = state.sampleQuestion

        // Recording indicator: visible only while recording (Requirements 3.4, 4.7)
        binding.recordingIndicator.visibility =
            if (state.isRecording) View.VISIBLE else View.GONE

        // Record button: visible when permissions granted; enabled when idle and not saving
        // (Requirements 3.1, 3.5, 4.1)
        binding.btnRecord.visibility =
            if (state.permissionsGranted) View.VISIBLE else View.GONE
        binding.btnRecord.isEnabled =
            state.permissionsGranted && !state.isRecording && !state.isSaving

        // Stop button: visible only while recording (Requirements 4.1, 4.2)
        binding.btnStop.visibility =
            if (state.isRecording) View.VISIBLE else View.GONE
        binding.btnStop.isEnabled = state.isRecording && !state.isSaving

        // Permission rationale (Requirement 2.3)
        if (!state.permissionsGranted) {
            Snackbar.make(
                binding.root,
                "Camera and microphone access are required to record an answer.",
                Snackbar.LENGTH_LONG
            ).show()
        }

        // Navigate to report fragment on successful save
        state.savedFilename?.let { filename ->
            viewModel.clearSavedFilename()
            val bundle = Bundle().apply {
                putString("filename", filename)
            }
            findNavController().navigate(R.id.action_recordingFragment_to_reportFragment, bundle)
        }

        // Error message (Requirements 3.3, 3.7, 4.5)
        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }
}
