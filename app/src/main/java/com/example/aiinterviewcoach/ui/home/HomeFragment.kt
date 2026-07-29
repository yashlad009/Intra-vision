package com.example.aiinterviewcoach.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tap the Resume card to navigate to Resume tab
        binding.cvResumeStatus.setOnClickListener {
            findNavController().navigate(R.id.nav_resume)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.tvStreakCount.text = "${state.streakDays} ${if (state.streakDays == 1) "Day" else "Days"}"
                binding.tvXpDisplay.text = "${state.totalXp} XP"

                binding.tvQuantPercent.text = "${state.quantitativeProgress}%"
                binding.pbQuantitative.progress = state.quantitativeProgress

                binding.tvLogicalPercent.text = "${state.logicalProgress}%"
                binding.pbLogical.progress = state.logicalProgress

                binding.tvVerbalPercent.text = "${state.verbalProgress}%"
                binding.pbVerbal.progress = state.verbalProgress

                binding.tvTotalSessionsCount.text = state.totalSessions.toString()
                binding.tvLastSessionText.text = state.lastSessionTimeFormatted

                // Resume card teaser text
                binding.tvResumeStatusTeaser.text = if (state.isResumeUploaded) {
                    "Resume uploaded ✓"
                } else {
                    "Not uploaded yet"
                }
                binding.tvResumeStatusTeaser.setTextColor(
                    if (state.isResumeUploaded) {
                        requireContext().getColor(R.color.primary)
                    } else {
                        requireContext().getColor(R.color.text_secondary)
                    }
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
