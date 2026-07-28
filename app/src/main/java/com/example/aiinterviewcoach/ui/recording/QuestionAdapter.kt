package com.example.aiinterviewcoach.ui.recording

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aiinterviewcoach.databinding.ItemQuestionBinding
import com.example.aiinterviewcoach.model.Question

class QuestionAdapter(
    private val onQuestionClicked: (Question) -> Unit
) : ListAdapter<Question, QuestionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question) {
            binding.tvQuestionText.text = question.text
            binding.tvQuestionTip.text = question.tip
            binding.root.setOnClickListener {
                onQuestionClicked(question)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean =
            oldItem == newItem
    }
}
