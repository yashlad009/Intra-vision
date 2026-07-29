package com.example.aiinterviewcoach.ui.aptitude

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentPracticeQaBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PracticeQAFragment : Fragment() {

    private val viewModel: PracticeQAViewModel by viewModels()
    private val args: PracticeQAFragmentArgs by navArgs()
    private var _binding: FragmentPracticeQaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPracticeQaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = args.category
        val topicId = args.topicId

        viewModel.loadQuestions(category, topicId)

        val flipClickListener = View.OnClickListener {
            viewModel.flipCard()
        }
        binding.cvFlipCard.setOnClickListener(flipClickListener)
        binding.llCardContentContainer.setOnClickListener(flipClickListener)
        binding.llCardFront.setOnClickListener(flipClickListener)
        binding.llCardBack.setOnClickListener(flipClickListener)

        binding.btnNextQuestion.setOnClickListener {
            viewModel.nextQuestion()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state.showFinishedScreen) {
                    Toast.makeText(requireContext(), "Practice Completed! +10 XP Awarded", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                    return@collect
                }

                if (state.questions.isEmpty()) {
                    binding.tvQuestionText.text = "No questions found for this topic."
                    binding.btnNextQuestion.isEnabled = false
                    return@collect
                }

                val currentQuestion = state.questions[state.currentIndex]
                val total = state.questions.size
                val currentNum = state.currentIndex + 1

                // Update counter and progress bar
                binding.tvCounter.text = "Question $currentNum of $total"
                binding.pbPractice.progress = (currentNum * 100) / total

                // Update button text on last question
                if (currentNum == total) {
                    binding.btnNextQuestion.text = "Finish Practice"
                } else {
                    binding.btnNextQuestion.text = "Next Question"
                }

                // Render Front and Back contents
                binding.tvQuestionText.text = currentQuestion.question
                binding.tvAnswerText.text = currentQuestion.answer
                binding.tvExplanationText.text = currentQuestion.explanation

                // Toggle visibility based on flip state
                if (state.isFlipped) {
                    binding.llCardFront.visibility = View.GONE
                    binding.llCardBack.visibility = View.VISIBLE
                } else {
                    binding.llCardFront.visibility = View.VISIBLE
                    binding.llCardBack.visibility = View.GONE
                }

                // Render options
                binding.llOptionsContainer.removeAllViews()
                if (currentQuestion.options.isNotEmpty()) {
                    for (opt in currentQuestion.options) {
                        val textView = TextView(requireContext()).apply {
                            text = opt
                            textSize = 15f
                            setTextColor(Color.parseColor("#475569")) // Slate-600
                            setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, dpToPx(8), 0, dpToPx(8))
                            }
                            layoutParams = params
                            gravity = Gravity.CENTER_VERTICAL
                            // Border & rounded background
                            setBackgroundResource(R.drawable.bg_option_outline)
                        }
                        binding.llOptionsContainer.addView(textView)
                    }
                }
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
