package com.example.aiinterviewcoach.ui.aptitude

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentMockTestResultBinding
import com.example.aiinterviewcoach.databinding.ItemMockTestReviewBinding
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray

data class MockReviewItem(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val userAnswer: String,
    val explanation: String
)

@AndroidEntryPoint
class MockTestResultFragment : Fragment() {

    private val args: MockTestResultFragmentArgs by navArgs()
    private var _binding: FragmentMockTestResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMockTestResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val total = args.totalQuestions
        val correct = args.correctAnswers
        val pct = if (total > 0) (correct.toDouble() / total.toDouble() * 100).toInt() else 0
        val timeSec = args.timeTakenSeconds

        binding.tvScorePercentage.text = "$pct%"
        binding.tvScoreFraction.text = "$correct of $total Correct"

        val minutes = timeSec / 60
        val seconds = timeSec % 60
        binding.tvTimeTaken.text = String.format("Time Taken: %02d:%02d", minutes, seconds)

        // Parse questions JSON for review
        val reviewItems = mutableListOf<MockReviewItem>()
        try {
            val jsonArray = JSONArray(args.questionsJsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val question = obj.optString("question")
                val correctAnswer = obj.optString("answer")
                val explanation = obj.optString("explanation")
                val userAnswer = obj.optString("userAnswer")

                val optsJson = obj.optJSONArray("options")
                val options = mutableListOf<String>()
                if (optsJson != null) {
                    for (j in 0 until optsJson.length()) {
                        options.add(optsJson.getString(j))
                    }
                }
                reviewItems.add(MockReviewItem(question, options, correctAnswer, userAnswer, explanation))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.rvReviewList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviewList.adapter = MockReviewAdapter(reviewItems)

        binding.btnBackToDashboard.setOnClickListener {
            // Pop back to the main Aptitude Dashboard
            findNavController().popBackStack(R.id.aptitudeFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class MockReviewAdapter(private val items: List<MockReviewItem>) :
    RecyclerView.Adapter<MockReviewAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMockTestReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMockTestReviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvReviewQuestion.text = item.question
        holder.binding.tvUserAnswer.text = if (item.userAnswer.isEmpty()) "Unanswered" else item.userAnswer
        holder.binding.tvCorrectAnswer.text = item.correctAnswer
        holder.binding.tvReviewExplanation.text = item.explanation

        val isCorrect = item.userAnswer.trim().lowercase() == item.correctAnswer.trim().lowercase()
        if (isCorrect) {
            holder.binding.tvUserAnswer.setTextColor(Color.parseColor("#16A34A")) // Green
        } else {
            holder.binding.tvUserAnswer.setTextColor(Color.parseColor("#E11D48")) // Red
        }
    }

    override fun getItemCount(): Int = items.size
}
