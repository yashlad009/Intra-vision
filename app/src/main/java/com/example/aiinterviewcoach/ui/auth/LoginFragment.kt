package com.example.aiinterviewcoach.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrEmpty()) {
                    viewModel.signInWithGoogle(idToken)
                } else {
                    Toast.makeText(requireContext(), "Google Sign-In failed: No ID Token retrieved.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if user is already logged in
        if (viewModel.isLoggedIn) {
            navigateToDashboard()
            return
        }

        setupGoogleSignIn()
        setupClickListeners()
        observeState()
    }

    private fun setupGoogleSignIn() {
        val webClientId = getString(R.string.default_web_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.signInWithEmail(email, password)
        }

        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.btnGuestSignIn.setOnClickListener {
            viewModel.signInAsGuest()
        }

        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            viewModel.sendPasswordReset(email)
        }

        binding.tvRegister.setOnClickListener {
            viewModel.resetUiState()
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
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
                            Toast.makeText(requireContext(), "Welcome ${state.user.displayName ?: state.user.email}!", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        }
                        is AuthUiState.Error -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        is AuthUiState.PasswordResetSent -> {
                            binding.loadingOverlay.visibility = View.GONE
                            Toast.makeText(requireContext(), "Password reset email sent! Check your inbox.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun navigateToDashboard() {
        findNavController().navigate(R.id.action_loginFragment_to_landingFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
