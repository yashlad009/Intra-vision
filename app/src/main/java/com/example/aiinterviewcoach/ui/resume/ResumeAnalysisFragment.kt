package com.example.aiinterviewcoach.ui.resume

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentResumeAnalysisBinding
import com.example.aiinterviewcoach.scoring.ResumeAnalysisResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ResumeAnalysisFragment : Fragment() {

    private val viewModel: ResumeViewModel by viewModels()
    private var _binding: FragmentResumeAnalysisBinding? = null
    private val binding get() = _binding!!

    private lateinit var checkAdapter: ResumeCheckAdapter
    private lateinit var recommendationAdapter: ResumeRecommendationAdapter

    private val pdfPickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val fileName = getFileNameFromUri(selectedUri)
            viewModel.processPdfUri(selectedUri, fileName)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResumeAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnReupload.setOnClickListener {
            pdfPickerLauncher.launch(arrayOf("application/pdf"))
        }

        // Observe latest resume entity and populate analysis results
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.latestResume.collectLatest { resume ->
                if (resume != null) {
                    binding.tvAnalysisFileName.text = resume.fileName

                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val dateStr = sdf.format(Date(resume.uploadedAt))
                    binding.tvAnalysisTimestamp.text = "Analyzed locally on $dateStr"

                    val analysisResult = ResumeAnalysisResult.fromJson(resume.analysisJson)
                    binding.tvOverallScore.text = "${analysisResult.totalScore}%"

                    checkAdapter.submitList(analysisResult.checks)

                    if (analysisResult.recommendations.isNotEmpty()) {
                        binding.tvRecommendationsHeader.visibility = View.VISIBLE
                        binding.tvRecommendationsSub.visibility = View.VISIBLE
                        binding.rvRecommendations.visibility = View.VISIBLE
                        recommendationAdapter.submitList(analysisResult.recommendations)
                    } else {
                        binding.tvRecommendationsHeader.visibility = View.GONE
                        binding.tvRecommendationsSub.visibility = View.GONE
                        binding.rvRecommendations.visibility = View.GONE
                    }
                }
            }
        }

        // Observe error messages
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collectLatest { errorMsg ->
                errorMsg?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun setupRecyclerViews() {
        checkAdapter = ResumeCheckAdapter()
        binding.rvChecks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChecks.adapter = checkAdapter

        recommendationAdapter = ResumeRecommendationAdapter { recommendationItem ->
            val bundle = Bundle().apply {
                putString("category", recommendationItem.category)
            }
            try {
                findNavController().navigate(R.id.questionSelectFragment, bundle)
            } catch (e: Exception) {
                // Fallback to navigating via main graph
                Toast.makeText(requireContext(), "Navigating to ${recommendationItem.title}...", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvRecommendations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecommendations.adapter = recommendationAdapter
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var name = "resume.pdf"
        try {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        name = it.getString(displayNameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            name = uri.lastPathSegment ?: "resume.pdf"
        }
        return name
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
