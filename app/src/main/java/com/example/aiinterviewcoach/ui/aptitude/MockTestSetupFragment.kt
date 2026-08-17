package com.example.aiinterviewcoach.ui.aptitude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.aiinterviewcoach.R
import com.example.aiinterviewcoach.databinding.FragmentMockTestSetupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MockTestSetupFragment : Fragment() {

    private var _binding: FragmentMockTestSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMockTestSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStartTest.setOnClickListener {
            val category = when (binding.cgCategory.checkedChipId) {
                R.id.chipQuant -> "quantitative"
                R.id.chipLogical -> "logical"
                R.id.chipVerbal -> "verbal"
                else -> "mixed"
            }

            val questionCount = when (binding.cgQuestionCount.checkedChipId) {
                R.id.chipCount15 -> 15
                R.id.chipCount25 -> 25
                else -> 40
            }

            val action = MockTestSetupFragmentDirections.actionMockTestSetupFragmentToMockTestFragment(
                category = category,
                questionCount = questionCount
            )
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
