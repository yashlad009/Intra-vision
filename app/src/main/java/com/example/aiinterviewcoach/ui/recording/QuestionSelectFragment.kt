package com.example.aiinterviewcoach.ui.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aiinterviewcoach.databinding.FragmentQuestionSelectBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestionSelectFragment : Fragment() {

    private val viewModel: QuestionSelectViewModel by viewModels()
    private val args: QuestionSelectFragmentArgs by navArgs()
    private var _binding: FragmentQuestionSelectBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: QuestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set screen title/desc based on selected category
        val categoryFormatted = args.category.lowercase().replaceFirstChar { it.uppercase() }
        binding.tvCategoryTitle.text = "$categoryFormatted Questions"
        binding.tvCategoryDesc.text = when (args.category.uppercase()) {
            "HR" -> "Personal background, strengths, and motivational HR questions."
            "TECHNICAL" -> "Core computer science concepts, processes, threads, and databases."
            "BEHAVIORAL" -> "Use the STAR method (Situation, Task, Action, Result) to answer these scenario questions."
            else -> "Select a question to practice your response."
        }

        // Back navigation
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Set up recycler adapter
        adapter = QuestionAdapter { question ->
            val action = QuestionSelectFragmentDirections
                .actionQuestionSelectFragmentToRecordingFragment(questionText = question.text)
            findNavController().navigate(action)
        }
        binding.rvQuestions.adapter = adapter

        // Fetch questions
        viewModel.loadQuestionsByCategory(args.category)

        // Collect and submit questions
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.questions.collect { questionsList ->
                adapter.submitList(questionsList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
