package com.example.aiinterviewcoach.ui.recording

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aiinterviewcoach.databinding.ItemRecordingBinding
import com.example.aiinterviewcoach.model.RecordingEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingAdapter(
    private val isRecycleBinMode: Boolean = false,
    private val onItemClick: (RecordingEntry) -> Unit,
    private val onSoftDeleteClick: ((RecordingEntry) -> Unit)? = null,
    private val onRestoreClick: ((RecordingEntry) -> Unit)? = null,
    private val onPermanentDeleteClick: ((RecordingEntry) -> Unit)? = null
) : ListAdapter<RecordingEntry, RecordingAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemRecordingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: RecordingEntry) {
            binding.tvFilename.text = entry.filename.take(100)
            binding.tvOverallScore.text = "${entry.overallScore}%"

            val wpmText = if (entry.wordsPerMinute > 0) "${entry.wordsPerMinute} WPM" else "Speech Pace: ${entry.paceScore}%"
            val fillersText = if (entry.fillerWordCount > 0) "${entry.fillerWordCount} fillers" else "Clarity: ${entry.speechClarityScore}%"
            binding.tvMetricsSummary.text = "$wpmText  •  $fillersText  •  Posture: ${entry.postureScore}%"

            val dateMillis = if (isRecycleBinMode && entry.deletedAt != null) entry.deletedAt else entry.createdAt
            val prefix = if (isRecycleBinMode) "Deleted: " else "Recorded: "
            binding.tvDate.text = prefix + formatDate(dateMillis)

            if (isRecycleBinMode) {
                binding.btnSoftDelete.visibility = View.GONE
                binding.btnRestore.visibility = View.VISIBLE
                binding.btnPermanentDelete.visibility = View.VISIBLE

                binding.btnRestore.setOnClickListener { onRestoreClick?.invoke(entry) }
                binding.btnPermanentDelete.setOnClickListener { onPermanentDeleteClick?.invoke(entry) }
            } else {
                binding.btnSoftDelete.visibility = View.VISIBLE
                binding.btnRestore.visibility = View.GONE
                binding.btnPermanentDelete.visibility = View.GONE

                binding.btnSoftDelete.setOnClickListener { onSoftDeleteClick?.invoke(entry) }
                binding.root.setOnClickListener { onItemClick(entry) }
            }
        }
    }

    private fun formatDate(timestampMs: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }

    private class DiffCallback : DiffUtil.ItemCallback<RecordingEntry>() {
        override fun areItemsTheSame(oldItem: RecordingEntry, newItem: RecordingEntry): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: RecordingEntry, newItem: RecordingEntry): Boolean =
            oldItem == newItem
    }
}
