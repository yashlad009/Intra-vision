package com.example.aiinterviewcoach.ui.aptitude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.aiinterviewcoach.databinding.FragmentAptitudeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AptitudeFragment : Fragment() {

    private val viewModel: AptitudeViewModel by viewModels()
    private var _binding: FragmentAptitudeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAptitudeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe progress and stats
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.tvStreakCount.text = "${state.streakDays} ${if (state.streakDays == 1) "Day" else "Days"}"
                binding.tvXpDisplay.text = "${state.totalXp} XP"
                binding.pbQuantitative.progress = state.quantitativeProgress
                binding.pbLogical.progress = state.logicalProgress
                binding.pbVerbal.progress = state.verbalProgress
            }
        }

        // Set up click listeners for Stage Cards
        binding.cvPrepareCard.setOnClickListener {
            val isVisible = binding.llPrepareSubCategories.visibility == View.VISIBLE
            androidx.transition.TransitionManager.beginDelayedTransition(binding.root as android.view.ViewGroup)
            if (isVisible) {
                binding.llPrepareSubCategories.visibility = View.GONE
                binding.ivPrepareChevron.setImageResource(android.R.drawable.arrow_down_float)
            } else {
                binding.llPrepareSubCategories.visibility = View.VISIBLE
                binding.ivPrepareChevron.setImageResource(android.R.drawable.arrow_up_float)
            }
        }

        binding.cvPracticeCard.setOnClickListener {
            val isVisible = binding.hsvPracticeSessions.visibility == View.VISIBLE
            androidx.transition.TransitionManager.beginDelayedTransition(binding.root as android.view.ViewGroup)
            if (isVisible) {
                binding.hsvPracticeSessions.visibility = View.GONE
                binding.ivPracticeChevron.setImageResource(android.R.drawable.arrow_down_float)
            } else {
                binding.hsvPracticeSessions.visibility = View.VISIBLE
                binding.ivPracticeChevron.setImageResource(android.R.drawable.arrow_up_float)
            }
        }

        binding.cvConquerCard.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Conquer mode (Timed Mock Test) is coming soon!", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.cvQuantitative.setOnClickListener {
            navigateToTopicList("quantitative")
        }

        binding.cvLogical.setOnClickListener {
            navigateToTopicList("logical")
        }

        binding.cvVerbal.setOnClickListener {
            navigateToTopicList("verbal")
        }

        binding.cvPracticeQuantitative.setOnClickListener {
            navigateToPractice("quantitative", "All Quantitative")
        }

        binding.cvPracticeLogical.setOnClickListener {
            navigateToPractice("logical", "All Logical")
        }

        binding.cvPracticeVerbal.setOnClickListener {
            navigateToPractice("verbal", "All Verbal")
        }
    }

    private fun navigateToPractice(category: String, topicName: String) {
        val action = AptitudeFragmentDirections.actionAptitudeFragmentToPracticeQAFragment(
            category = category,
            topicId = "all",
            topicName = topicName
        )
        findNavController().navigate(action)
    }

    private fun navigateToTopicList(category: String) {
        val action = AptitudeFragmentDirections.actionAptitudeFragmentToTopicListFragment(category = category)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
