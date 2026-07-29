package com.example.aiinterviewcoach.ui.aptitude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aiinterviewcoach.data.local.AptitudeRepository
import com.example.aiinterviewcoach.databinding.FragmentStudyNoteBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class StudyNoteFragment : Fragment() {

    @Inject
    lateinit var repository: AptitudeRepository

    private val args: StudyNoteFragmentArgs by navArgs()
    private var _binding: FragmentStudyNoteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = args.category
        val topicId = args.topicId
        val topicName = args.topicName

        // Mark as opened
        viewLifecycleOwner.lifecycleScope.launch {
            repository.recordTopicOpened(category, topicId)
        }

        // Render Markdown using Markwon
        val markwon = Markwon.create(requireContext())
        val mdFileName = "prepare/$category/$topicId.md"
        
        try {
            val markdownContent = requireContext().assets.open(mdFileName).bufferedReader().use { it.readText() }
            
            // Parse sections
            val sections = MarkdownSectionParser.parse(markdownContent)
            val (learn, practice, revise) = MarkdownSectionParser.groupSections(sections)

            // Setup ViewPager adapter
            val adapter = AptitudeStudyPagerAdapter(learn, practice, revise, markwon)
            binding.viewPager.adapter = adapter

            // Link TabLayout and ViewPager2
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Learn"
                    1 -> "Practice"
                    else -> "Revise"
                }
            }.attach()

        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error loading study notes.", Toast.LENGTH_SHORT).show()
        }

        // Mark completed button listener
        binding.btnMarkCompleted.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                repository.markTopicCompleted(category, topicId)
                Toast.makeText(requireContext(), "Topic Completed! +5 XP", Toast.LENGTH_SHORT).show()
                binding.btnMarkCompleted.isEnabled = false
                binding.btnMarkCompleted.text = "Completed ✓"
            }
        }

        // Practice button listener
        binding.btnPractice.setOnClickListener {
            val action = StudyNoteFragmentDirections.actionStudyNoteFragmentToPracticeQAFragment(
                category = category,
                topicId = topicId,
                topicName = topicName
            )
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
