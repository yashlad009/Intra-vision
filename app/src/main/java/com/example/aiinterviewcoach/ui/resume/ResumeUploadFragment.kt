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
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentResumeUploadBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ResumeUploadFragment : Fragment() {

    private val viewModel: ResumeViewModel by viewModels()
    private var _binding: FragmentResumeUploadBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentResumeUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUploadPdf.setOnClickListener {
            pdfPickerLauncher.launch(arrayOf("application/pdf"))
        }

        binding.btnReplaceResume.setOnClickListener {
            pdfPickerLauncher.launch(arrayOf("application/pdf"))
        }

        binding.btnViewAnalysis.setOnClickListener {
            findNavController().navigate(R.id.action_resumeUploadFragment_to_resumeAnalysisFragment)
        }

        // Observe extraction loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isExtracting.collectLatest { isExtracting ->
                if (isExtracting) {
                    binding.cvLoadingState.visibility = View.VISIBLE
                    binding.cvEmptyState.visibility = View.GONE
                    binding.cvUploadedState.visibility = View.GONE
                } else {
                    binding.cvLoadingState.visibility = View.GONE
                    updateStateViews()
                }
            }
        }

        // Observe stored resume entity
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.latestResume.collectLatest {
                updateStateViews()
            }
        }

        // Observe navigation event after parsing completes
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigateToAnalysis.collect {
                findNavController().navigate(R.id.action_resumeUploadFragment_to_resumeAnalysisFragment)
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

    private fun updateStateViews() {
        if (viewModel.isExtracting.value) return

        val resume = viewModel.latestResume.value
        if (resume != null) {
            binding.cvEmptyState.visibility = View.GONE
            binding.cvUploadedState.visibility = View.VISIBLE

            binding.tvFileName.text = resume.fileName

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date(resume.uploadedAt))
            binding.tvUploadDate.text = "Uploaded on $dateStr"
        } else {
            binding.cvEmptyState.visibility = View.VISIBLE
            binding.cvUploadedState.visibility = View.GONE
        }
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
