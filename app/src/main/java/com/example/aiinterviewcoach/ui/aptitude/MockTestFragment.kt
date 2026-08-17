package com.example.aiinterviewcoach.ui.aptitude

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentMockTestBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject

@AndroidEntryPoint
class MockTestFragment : Fragment() {

    private val viewModel: MockTestViewModel by viewModels()
    private val args: MockTestFragmentArgs by navArgs()
    private var _binding: FragmentMockTestBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMockTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = args.category
        val questionCount = args.questionCount

        viewModel.startTest(category, questionCount)

        binding.btnTestPrev.setOnClickListener {
            viewModel.previousQuestion()
        }

        binding.btnTestNext.setOnClickListener {
            val state = viewModel.state.value
            if (state.currentIndex == state.questions.lastIndex) {
                viewModel.submitTest()
            } else {
                viewModel.nextQuestion()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                if (state.isFinished) {
                    // Navigate to Result screen with serialized questions and user selections
                    val questionsArray = org.json.JSONArray()
                    state.questions.forEachIndexed { idx, q ->
                        val qObj = org.json.JSONObject()
                        qObj.put("question", q.question)
                        qObj.put("answer", q.answer)
                        qObj.put("explanation", q.explanation)
                        val optsArr = org.json.JSONArray()
                        q.options.forEach { optsArr.put(it) }
                        qObj.put("options", optsArr)
                        qObj.put("userAnswer", state.selectedAnswers[idx] ?: "")
                        questionsArray.put(qObj)
                    }

                    Toast.makeText(requireContext(), "Mock Test Submitted! +20 XP", Toast.LENGTH_LONG).show()

                    val action = MockTestFragmentDirections.actionMockTestFragmentToMockTestResultFragment(
                        category = category,
                        totalQuestions = state.questions.size,
                        correctAnswers = state.correctAnswersCount,
                        timeTakenSeconds = state.totalTimeTakenSeconds,
                        questionsJsonString = questionsArray.toString()
                    )
                    findNavController().navigate(action)
                    return@collect
                }

                if (state.questions.isEmpty()) {
                    binding.tvTestQuestionText.text = "Loading questions..."
                    binding.btnTestNext.isEnabled = false
                    binding.btnTestPrev.isEnabled = false
                    return@collect
                }

                val currentQuestion = state.questions[state.currentIndex]
                val currentNum = state.currentIndex + 1
                val total = state.questions.size

                // Update text displays
                binding.tvProgressCounter.text = "Question $currentNum of $total"
                binding.pbTestProgress.progress = (currentNum * 100) / total
                binding.tvTestQuestionText.text = currentQuestion.question

                // Format timer display
                val minutes = state.timeRemainingSeconds / 60
                val seconds = state.timeRemainingSeconds % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)

                // Update buttons state
                binding.btnTestPrev.isEnabled = state.currentIndex > 0
                binding.btnTestPrev.visibility = if (state.currentIndex > 0) View.VISIBLE else View.INVISIBLE

                if (state.currentIndex == state.questions.lastIndex) {
                    binding.btnTestNext.text = "Submit Test"
                } else {
                    binding.btnTestNext.text = "Next"
                }

                // Render options
                binding.rgOptions.setOnCheckedChangeListener(null)
                binding.rgOptions.removeAllViews()

                val userSelection = state.selectedAnswers[state.currentIndex]

                currentQuestion.options.forEachIndexed { optIndex, opt ->
                    val radioButton = RadioButton(requireContext()).apply {
                        id = optIndex
                        text = opt
                        textSize = 15f
                        setTextColor(Color.parseColor("#475569")) // Slate-600
                        buttonTintList = ColorStateList.valueOf(Color.parseColor("#0F6E56"))
                        setPadding(12, 16, 12, 16)
                        layoutParams = RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.MATCH_PARENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, 8, 0, 8)
                        }
                        setBackgroundResource(R.drawable.bg_option_outline)
                        isChecked = (opt == userSelection)
                    }
                    binding.rgOptions.addView(radioButton)
                }

                binding.rgOptions.setOnCheckedChangeListener { _, checkedId ->
                    if (checkedId != -1 && checkedId < currentQuestion.options.size) {
                        val chosenOption = currentQuestion.options[checkedId]
                        viewModel.selectAnswer(chosenOption)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
