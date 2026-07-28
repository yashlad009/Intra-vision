package com.example.aiinterviewcoach.ui.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.aiinterviewcoach.databinding.FragmentHistoryBinding
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private val viewModel: HistoryViewModel by viewModels()
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var activeAdapter: RecordingAdapter
    private lateinit var trashAdapter: RecordingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupTabs()
        observeViewModel()
    }

    private fun setupAdapters() {
        activeAdapter = RecordingAdapter(
            isRecycleBinMode = false,
            onItemClick = { entry ->
                val action = HistoryFragmentDirections.actionHistoryFragmentToPlaybackFragment(entry.filename)
                findNavController().navigate(action)
            },
            onSoftDeleteClick = { entry ->
                viewModel.softDelete(entry)
            }
        )

        trashAdapter = RecordingAdapter(
            isRecycleBinMode = true,
            onItemClick = {},
            onRestoreClick = { entry ->
                viewModel.restore(entry)
            },
            onPermanentDeleteClick = { entry ->
                confirmPermanentDelete(entry)
            }
        )

        binding.rvRecordings.adapter = activeAdapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.selectTab(HistoryTab.ACTIVE)
                    1 -> viewModel.selectTab(HistoryTab.RECYCLE_BIN)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.btnEmptyTrash.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Empty Recycle Bin?")
                .setMessage("All items in the Recycle Bin will be permanently deleted and freed from device storage. This action cannot be undone.")
                .setPositiveButton("Empty Trash") { _, _ ->
                    viewModel.emptyRecycleBin()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.storageUsage.collect { usageText ->
                binding.tvStorageMeter.text = "Storage Used: $usageText"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.studentOverallScore.collect { score ->
                binding.tvStudentOverallScore.text = "Student Score: $score%"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.studentRatingBadge.collect { badge ->
                binding.tvStudentRatingBadge.text = badge
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentTab.collect { tab ->
                when (tab) {
                    HistoryTab.ACTIVE -> {
                        binding.rvRecordings.adapter = activeAdapter
                        binding.btnEmptyTrash.visibility = View.GONE
                        updateListDisplay(viewModel.activeEntries.value, isTrash = false)
                    }
                    HistoryTab.RECYCLE_BIN -> {
                        binding.rvRecordings.adapter = trashAdapter
                        val trashEntries = viewModel.recycleBinEntries.value
                        binding.btnEmptyTrash.visibility = if (trashEntries.isNotEmpty()) View.VISIBLE else View.GONE
                        updateListDisplay(trashEntries, isTrash = true)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activeEntries.collect { entries ->
                if (viewModel.currentTab.value == HistoryTab.ACTIVE) {
                    updateListDisplay(entries, isTrash = false)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recycleBinEntries.collect { entries ->
                if (viewModel.currentTab.value == HistoryTab.RECYCLE_BIN) {
                    binding.btnEmptyTrash.visibility = if (entries.isNotEmpty()) View.VISIBLE else View.GONE
                    updateListDisplay(entries, isTrash = true)
                }
            }
        }
    }

    private fun updateListDisplay(entries: List<com.example.aiinterviewcoach.model.RecordingEntry>, isTrash: Boolean) {
        if (entries.isEmpty()) {
            binding.rvRecordings.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.tvEmptyState.text = if (isTrash) "Recycle Bin is empty" else "No active practice sessions yet"
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvRecordings.visibility = View.VISIBLE
            if (isTrash) {
                trashAdapter.submitList(entries)
            } else {
                activeAdapter.submitList(entries)
            }
        }
    }

    private fun confirmPermanentDelete(entry: com.example.aiinterviewcoach.model.RecordingEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Permanently?")
            .setMessage("This video file (${entry.filename}) will be permanently deleted from your phone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePermanently(entry)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateStorageUsage()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
