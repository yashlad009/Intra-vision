package com.example.aiinterviewcoach.ui.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.aiinterviewcoach.ui.auth.AuthViewModel
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentLandingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LandingFragment : Fragment() {

    private val viewModel: LandingViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var _binding: FragmentLandingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Session check: If user is not logged in, navigate to LoginFragment
        if (!authViewModel.isLoggedIn) {
            findNavController().navigate(R.id.action_landingFragment_to_loginFragment)
            return
        }

        // Display current user email or display name
        val user = authViewModel.currentUser
        val displayName = user?.displayName ?: user?.email ?: "User"
        binding.tvUserEmail.text = "Hello, $displayName"

        // Logout listener
        binding.btnLogout.setOnClickListener {
            authViewModel.signOut()
            findNavController().navigate(R.id.action_landingFragment_to_loginFragment)
        }

        // Observe dashboard UI State
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.tvTotalSessionsCount.text = state.totalSessions.toString()
                binding.tvLastSessionText.text = state.lastSessionTimeFormatted
                binding.tvStreakCount.text = state.streakText
            }
        }

        // Setup click listeners for categories
        binding.cvHr.setOnClickListener {
            val action = LandingFragmentDirections.actionLandingFragmentToQuestionSelectFragment(category = "HR")
            findNavController().navigate(action)
        }

        binding.cvTechnical.setOnClickListener {
            val action = LandingFragmentDirections.actionLandingFragmentToQuestionSelectFragment(category = "TECHNICAL")
            findNavController().navigate(action)
        }

        binding.cvBehavioral.setOnClickListener {
            val action = LandingFragmentDirections.actionLandingFragmentToQuestionSelectFragment(category = "BEHAVIORAL")
            findNavController().navigate(action)
        }

        // Setup click listeners for Quick Actions
        binding.btnViewHistory.setOnClickListener {
            val action = LandingFragmentDirections.actionLandingFragmentToHistoryFragment()
            findNavController().navigate(action)
        }

        binding.btnQuickPractice.setOnClickListener {
            val questionText = viewModel.getRandomQuestionText()
            val action = LandingFragmentDirections.actionLandingFragmentToRecordingFragment(questionText = questionText)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
