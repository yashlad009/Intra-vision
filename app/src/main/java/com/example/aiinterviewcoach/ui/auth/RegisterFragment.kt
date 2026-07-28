package com.example.aiinterviewcoach.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val passwordConfirm = binding.etPasswordConfirm.text.toString().trim()

            viewModel.signUpWithEmail(name, email, password, passwordConfirm)
        }

        binding.tvLogin.setOnClickListener {
            viewModel.resetUiState()
            findNavController().popBackStack()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AuthUiState.Idle -> {
                            binding.loadingOverlay.visibility = View.GONE
                        }
                        is AuthUiState.Loading -> {
                            binding.loadingOverlay.visibility = View.VISIBLE
                        }
                        is AuthUiState.Success -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        }
                        is AuthUiState.Error -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun navigateToDashboard() {
        findNavController().navigate(R.id.action_registerFragment_to_landingFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
