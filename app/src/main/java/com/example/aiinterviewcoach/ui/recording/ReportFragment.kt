package com.example.aiinterviewcoach.ui.recording

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentReportBinding
import com.example.aiinterviewcoach.model.RecordingEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ReportFragment : Fragment() {

    private val viewModel: ReportViewModel by viewModels()
    private val args: ReportFragmentArgs by navArgs()
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, viewSet: Bundle?) {
        super.onViewCreated(view, viewSet)

        viewModel.loadSessionReport(args.filename)

        viewModel.uiState
            .onEach { state -> renderUi(state) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        binding.btnWatchPlayback.setOnClickListener {
            val bundle = Bundle().apply {
                putString("filename", args.filename)
            }
            findNavController().navigate(R.id.action_reportFragment_to_playbackFragment, bundle)
        }

        binding.btnDone.setOnClickListener {
            // Pop back to landing fragment
            findNavController().popBackStack(R.id.landingFragment, false)
        }
    }

    private fun renderUi(state: ReportUiState) {
        val current = state.currentSession ?: return

        // Bind Current Session Stats
        binding.tvOverallScore.text = "${current.overallScore}%"
        binding.tvEyeContactScore.text = "${current.eyeContactScore}%"
        binding.tvPostureScore.text = "${current.postureScore}%"
        binding.tvSpeechScore.text = "${current.speechClarityScore}%"
        binding.tvPaceScore.text = "${current.paceScore}%"

        binding.tvRecommendations.text = state.recommendations

        // Set performance description text
        binding.tvPerformanceFeedback.text = when {
            current.overallScore >= 85 -> "Outstanding performance! You are fully interview-ready."
            current.overallScore >= 75 -> "Great effort! A few tweaks to body posture will perfect your delivery."
            else -> "Good practice session. Focus on the tips below to improve your score."
        }

        // Draw Progress History Chart
        setupProgressChart(state.pastSessions)
    }

    private fun setupProgressChart(sessions: List<RecordingEntry>) {
        val chart = binding.progressLineChart

        // Configure general settings
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.isScaleXEnabled = true
        chart.isScaleYEnabled = false

        // Style X Axis (Sessions sequence)
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.parseColor("#94A3B8") // Slate 400
            gridColor = Color.parseColor("#334155")  // Slate 700
            setDrawGridLines(true)
            granularity = 1f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "Run ${value.toInt() + 1}"
                }
            }
        }

        // Style Y Axis (Scores percentage)
        chart.axisLeft.apply {
            textColor = Color.parseColor("#94A3B8")
            gridColor = Color.parseColor("#334155")
            setDrawGridLines(true)
            axisMinimum = 0f
            axisMaximum = 100f
            granularity = 10f
        }
        chart.axisRight.isEnabled = false // Hide right axis for cleaner UI

        // Build dataset
        val entries = sessions.mapIndexed { index, session ->
            Entry(index.toFloat(), session.overallScore.toFloat())
        }

        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Progress").apply {
                color = Color.parseColor("#0D9488") // Teal 600 (Main theme primary color)
                setCircleColor(Color.parseColor("#0D9488"))
                lineWidth = 3.5f
                circleRadius = 5f
                setDrawCircleHole(true)
                circleHoleColor = Color.parseColor("#1E293B") // Dark slate background to blend card hole
                circleHoleRadius = 2.5f
                valueTextColor = Color.WHITE
                valueTextSize = 10f
                setDrawFilled(true)
                fillColor = Color.parseColor("#0D9488")
                fillAlpha = 35 // Light semi-transparent gradient shade
                mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth bezier curves
            }

            chart.data = LineData(dataSet)
            chart.animateY(800) // Vertical scale animation
            chart.invalidate()
        } else {
            chart.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
