package com.example.aiinterviewcoach.ui.aptitude

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aiinterviewcoach.databinding.ItemTopicBinding
import com.example.aiinterviewcoach.data.local.AptitudeTopic

class TopicAdapter(
    private val onTopicClick: (AptitudeTopic) -> Unit
) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

    private var items = listOf<TopicListItem>()

    fun submitList(newList: List<TopicListItem>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val binding = ItemTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class TopicViewHolder(private val binding: ItemTopicBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TopicListItem) {
            binding.tvTopicName.text = item.topic.name

            if (item.isCompleted) {
                binding.ivCompletionStatus.setImageResource(android.R.drawable.checkbox_on_background)
                binding.ivCompletionStatus.imageTintList = ColorStateList.valueOf(Color.parseColor("#0F6E56"))
            } else {
                binding.ivCompletionStatus.setImageResource(android.R.drawable.checkbox_off_background)
                binding.ivCompletionStatus.imageTintList = ColorStateList.valueOf(Color.parseColor("#64748B"))
            }

            binding.root.setOnClickListener {
                onTopicClick(item.topic)
            }
        }
    }
}
