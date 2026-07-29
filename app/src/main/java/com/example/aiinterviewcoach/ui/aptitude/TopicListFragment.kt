package com.example.aiinterviewcoach.ui.aptitude

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aiinterviewcoach.databinding.FragmentTopicListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TopicListFragment : Fragment() {

    private val viewModel: TopicListViewModel by viewModels()
    private val args: TopicListFragmentArgs by navArgs()
    private var _binding: FragmentTopicListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TopicAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = args.category
        viewModel.loadTopics(category)

        // Set title header
        binding.tvCategoryName.text = formatCategoryTitle(category)

        // Set up recycler view
        adapter = TopicAdapter { topic ->
            val action = TopicListFragmentDirections.actionTopicListFragmentToStudyNoteFragment(
                category = topic.category,
                topicId = topic.id,
                topicName = topic.name
            )
            findNavController().navigate(action)
        }
        binding.rvTopics.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopics.adapter = adapter

        // Observe topics flow
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.topicItems.collect { items ->
                adapter.submitList(items)
            }
        }
    }

    private fun formatCategoryTitle(category: String): String {
        return when (category.lowercase()) {
            "quantitative" -> "Quantitative Aptitude"
            "logical" -> "Logical Reasoning"
            "verbal" -> "Verbal Ability"
            else -> category.replaceFirstChar { it.uppercase() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
